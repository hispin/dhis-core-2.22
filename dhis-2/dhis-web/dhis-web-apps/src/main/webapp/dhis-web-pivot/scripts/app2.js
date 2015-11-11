Ext.onReady( function() {
	var N = PT;

    // initialize
    (function() {
        var I = new N.Api.Instance(),
            requestManager = new N.Api.RequestManager(),
            manifestReq = $.getJSON('manifest.webapp'),
            systemInfoReq = $.getJSON('/api/system/info.json'),
            systemSettingsReq = $.getJSON('/api/systemSettings.json?key=keyCalendar&key=keyDateFormat&key=keyAnalysisRelativePeriod&key=keyHideUnapprovedDataInAnalytics'),
            userAccountReq = $.getJSON('/api/me/user-account.json');

        manifestReq.done(function(manifest) {
            I.manifest = manifest;

        systemInfoReq.done(function(systemInfo) {
            I.systemInfo = systemInfo;
            I.path = systemInfo.contextPath;

        systemSettingsReq.done(function(systemSettings) {
            I.systemSettings = systemSettings;

        userAccountReq.done(function(userAccount) {
            I.userAccount = userAccount;

        // calendar
        (function() {
            N.CalendarManager.setBaseUrl(I.getPath());
            N.CalendarManager.setDateFormat(I.getDateFormat());
            N.CalendarManager.generate(I.systemSettings.keyCalendar);
        })();

        });
        });
        });
        });
    })();
});
