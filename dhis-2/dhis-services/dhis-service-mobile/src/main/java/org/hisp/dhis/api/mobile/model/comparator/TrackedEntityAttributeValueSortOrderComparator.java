package org.hisp.dhis.api.mobile.model.comparator;

import java.util.Comparator;

import org.hisp.dhis.trackedentityattributevalue.TrackedEntityAttributeValue;

public class TrackedEntityAttributeValueSortOrderComparator implements Comparator<TrackedEntityAttributeValue>
{

        public int compare( TrackedEntityAttributeValue value0, TrackedEntityAttributeValue value1 )
        {
            if ( value0 == null || value1 == null || value0.getAttribute() == null || value1.getAttribute() == null )
            {
                return 0;
            }
            if ( value0.getAttribute().getSortOrderInListNoProgram() == null || value0.getAttribute().getSortOrderInListNoProgram() == 0 )
            {
                return value0.getAttribute().getName().compareTo( value1.getAttribute().getName() );
            }

            if ( value1.getAttribute().getSortOrderInListNoProgram() == null || value1.getAttribute().getSortOrderInListNoProgram() == 0 )
            {
                return value0.getAttribute().getName().compareTo( value1.getAttribute().getName() );
            }

            return value0.getAttribute().getSortOrderInListNoProgram() - value1.getAttribute().getSortOrderInListNoProgram();
        }

}
