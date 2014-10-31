package org.hisp.dhis.dataapproval;

import org.hisp.dhis.organisationunit.OrganisationUnit;
import org.hisp.dhis.setting.SystemSettingManager;
import org.hisp.dhis.user.CurrentUserService;
import org.hisp.dhis.user.User;

import java.util.HashMap;
import java.util.Map;

import static org.hisp.dhis.setting.SystemSettingManager.KEY_ACCEPTANCE_REQUIRED_FOR_APPROVAL;
import static org.hisp.dhis.setting.SystemSettingManager.KEY_HIDE_UNAPPROVED_DATA_IN_ANALYTICS;

/**
 * This package private class holds the context for deciding on data approval permissions.
 * The context contains both system settings and some qualities of the user.
 * <p>
 * This class is especially efficient if the settings are set once and
 * then used several times to generate ApprovalPermissions for different
 * DataApproval objects.
 *
 * @author Jim Grace
 */
class DataApprovalPermissionsEvaluator
{
    DataApprovalLevelService dataApprovalLevelService;

    User user;

    private boolean acceptanceRequiredForApproval;
    private boolean hideUnapprovedData;

    private boolean authorizedToApprove;
    private boolean authorizedToApproveAtLowerLevels;
    private boolean authorizedToAcceptAtLowerLevels;
    private boolean authorizedToViewUnapprovedData;

    private Map<OrganisationUnit, DataApprovalLevel> userOrgUnitApprovalLevelsCache = new HashMap<>();

    int maxApprovalLevel;

    private DataApprovalPermissionsEvaluator()
    {
    }

    /**
     * Allocates and populates the context for determining user permissions
     * on one or more DataApproval objects.
     *
     * @param currentUserService Current user service
     * @param systemSettingManager System setting manager
     * @param dataApprovalLevelService Data approval level service
     * @return context for determining user permissions
     */
    static DataApprovalPermissionsEvaluator makePermissionsEvaluator( CurrentUserService currentUserService,
            SystemSettingManager systemSettingManager, DataApprovalLevelService dataApprovalLevelService )
    {
        DataApprovalPermissionsEvaluator ev = new DataApprovalPermissionsEvaluator();

        ev.dataApprovalLevelService = dataApprovalLevelService;

        ev.user = currentUserService.getCurrentUser();

        ev.acceptanceRequiredForApproval = (Boolean) systemSettingManager.getSystemSetting( KEY_ACCEPTANCE_REQUIRED_FOR_APPROVAL, false );
        ev.hideUnapprovedData = (Boolean) systemSettingManager.getSystemSetting( KEY_HIDE_UNAPPROVED_DATA_IN_ANALYTICS, false );

        ev.authorizedToApprove = ev.user.getUserCredentials().isAuthorized( DataApproval.AUTH_APPROVE );
        ev.authorizedToApproveAtLowerLevels = ev.user.getUserCredentials().isAuthorized( DataApproval.AUTH_APPROVE_LOWER_LEVELS );
        ev.authorizedToAcceptAtLowerLevels = ev.user.getUserCredentials().isAuthorized( DataApproval.AUTH_ACCEPT_LOWER_LEVELS );
        ev.authorizedToViewUnapprovedData = ev.user.getUserCredentials().isAuthorized( DataApproval.AUTH_VIEW_UNAPPROVED_DATA );

        ev.maxApprovalLevel = dataApprovalLevelService.getAllDataApprovalLevels().size();

        tracePrint( "makePermissionsEvaluator acceptanceRequiredForApproval " + ev.acceptanceRequiredForApproval
                + " hideUnapprovedData " + ev.hideUnapprovedData + " authorizedToApprove " + ev.authorizedToApprove
                + " authorizedToAcceptAtLowerLevels " + ev.authorizedToAcceptAtLowerLevels
                + " authorizedToViewUnapprovedData " + ev.authorizedToViewUnapprovedData + " maxApprovalLevel " + ev.maxApprovalLevel );

        return ev;
    }

    /**
     * Allocates and fills a data approval permissions object according to
     * the context of system settings and user information.
     * <p>
     * If there is a data permissions state, also takes this into account.
     *
     * @param status the data approval status (if any)
     * @return the data approval permissions for the object
     */
    DataApprovalPermissions getPermissions( DataApprovalStatus status )
    {
        DataApproval da = status.getDataApproval();

        DataApprovalState s = status.getState();

        DataApprovalPermissions permissions = new DataApprovalPermissions();

        DataApprovalLevel userApprovalLevel = getUserOrgUnitApprovalLevel( da.getOrganisationUnit() );

        if ( userApprovalLevel == null )
        {
            return permissions; // Can't find user approval level, so no permissions are set.
        }

        int userLevel = userApprovalLevel.getLevel();

        int dataLevel = ( s.isApproved() ? da.getDataApprovalLevel().getLevel() : maxApprovalLevel );

        boolean mayApproveOrUnapproveAtLevel = ( authorizedToApprove && userLevel == dataLevel && !da.isAccepted() ) ||
                        ( authorizedToApproveAtLowerLevels && userLevel < dataLevel );

        boolean mayAcceptOrUnacceptAtLevel = authorizedToAcceptAtLowerLevels &&
                ( userLevel == dataLevel - 1 || ( userLevel < dataLevel && authorizedToApproveAtLowerLevels ) );

        boolean mayApprove = s.isApprovable() && mayApproveOrUnapproveAtLevel;

        boolean mayUnapprove = s.isUnapprovable() && ( ( mayApproveOrUnapproveAtLevel && !da.isAccepted() ) || mayAcceptOrUnacceptAtLevel );

        boolean mayAccept = s.isAcceptable() && mayAcceptOrUnacceptAtLevel;

        boolean mayUnaccept = s.isUnacceptable() && mayAcceptOrUnacceptAtLevel;

        boolean mayReadData = authorizedToViewUnapprovedData || !hideUnapprovedData || mayApprove
                || userLevel >= dataLevel;

        tracePrint( "getPermissions orgUnit " + ( da.getOrganisationUnit() == null ? "(null)" : da.getOrganisationUnit().getName() )
                + " combo " + da.getAttributeOptionCombo().getName() + " state " + s.name()
                + " isApproved " + s.isApproved()+ " isApprovable " + s.isApprovable()+ " isUnapprovable " + s.isUnapprovable()
                + " isAccepted " + s.isAccepted() + " isAcceptable " + s.isAcceptable() + " isUnacceptable " + s.isUnacceptable()
                + " userLevel " + userLevel + " dataLevel " + dataLevel
                + " mayApproveOrUnapproveAtLevel " + mayApproveOrUnapproveAtLevel + " mayAcceptOrUnacceptAtLevel " + mayAcceptOrUnacceptAtLevel
                + " mayApprove " + mayApprove + " mayUnapprove " + mayUnapprove
                + " mayAccept " + mayAccept + " mayUnaccept " + mayUnaccept
                + " mayReadData " + mayReadData );

        permissions.setMayApprove( mayApprove );
        permissions.setMayUnapprove( mayUnapprove );
        permissions.setMayAccept( mayAccept );
        permissions.setMayUnaccept( mayUnaccept );
        permissions.setMayReadData( mayReadData );

        return permissions;
    }

    private DataApprovalLevel getUserOrgUnitApprovalLevel( OrganisationUnit orgUnit )
    {
        DataApprovalLevel level = userOrgUnitApprovalLevelsCache.get( orgUnit );

        if ( level == null )
        {
            level = dataApprovalLevelService.getUserApprovalLevel( user, orgUnit, false );

            userOrgUnitApprovalLevelsCache.put( orgUnit, level );
        }

        return level;
    }

    private static void tracePrint( String s )
    {
//        System.out.println( s );
    }
}
