Ext.onReady( function() {
	var N = PT;

    // initialize
    (function() {
        var metaDataManager = N.MetaDataManager,
            calendarManager = N.CalendarManager,
            requestManager = new N.Api.RequestManager(),
            manifestReq = $.getJSON('manifest.webapp'),
            systemInfoReq = $.getJSON('/api/system/info.json'),
            systemSettingsReq = $.getJSON('/api/systemSettings.json?key=keyCalendar&key=keyDateFormat&key=keyAnalysisRelativePeriod&key=keyHideUnapprovedDataInAnalytics'),
            userAccountReq = $.getJSON('/api/me/user-account.json');

        manifestReq.done(function(manifest) {
            metaDataManager.manifest = manifest;

        systemInfoReq.done(function(systemInfo) {
            metaDataManager.systemInfo = systemInfo;
            metaDataManager.path = systemInfo.contextPath;

        systemSettingsReq.done(function(systemSettings) {
            metaDataManager.systemSettings = systemSettings;

        userAccountReq.done(function(userAccount) {
            metaDataManager.userAccount = userAccount;

            calendarManager.setBaseUrl(metaDataManager.getPath());
            calendarManager.setDateFormat(metaDataManager.getDateFormat());
            calendarManager.generate(metaDataManager.systemSettings.keyCalendar);

        // i18n
        requestManager.add(new N.Api.Request({
            baseUrl: 'i18n/i18n_app.properties',
            type: 'ajax',
            fn: function(r) {
                var t = this;

                N.I18nManager.add(dhis2.util.parseJavaProperties(r));

                if (metaDataManager.isUiLocaleDefault()) {
                    requestManager.ok(t);
                }
                else {
                    $.ajax({
                        url: 'i18n/i18n_app_' + metaDataManager.getUiLocale() + '.properties',
                        success: function(r) {
                            N.I18nManager.add(dhis2.util.parseJavaProperties(r));
                        },
                        error: function() {
                            console.log('(i18n) No translations found for system locale (' + metaDataManager.getUiLocale() + ')');
                        },
                        complete: function() {
                            requestManager.ok(t);
                        }
                    });
                }
            }
        }));

        // authorization
        requestManager.add(new N.Api.Request({
            baseUrl: metaDataManager.getPath() + '/api/me/authorization/F_VIEW_UNAPPROVED_DATA',
            fn: function(r) {
                metaDataManager.viewUnapprovedData = r;
                requestManager.ok(this);
            }
        }));

        // root nodes
        //requestManager.add(new N.Api.Request({
            //baseUrl: metaDataManager.getPath() + '/api/organisationUnits.json',
            //params: {
                //'userDataViewFallback': true,
                //'paging': false,
                //'fields': 'id,' + metaDataManager.getDisplayProperty() + ',children[id,' + namePropertyUrl + ']',
            //fn: function(r) {
                //metaDataManager.viewUnapprovedData = r;
                //requestManager.ok(this);
            //}
        //}));


        requestManager.run();


                                        //requests.push({
                                            //url: 'i18n/i18n_app.properties',
                                            //success: function(r) {
                                                //NS.i18n = dhis2.util.parseJavaProperties(r.responseText);

                                                //if (keyUiLocale === defaultKeyUiLocale) {
                                                    //fn();
                                                //}
                                                //else {
                                                    //Ext.Ajax.request({
                                                        //url: 'i18n/i18n_app_' + keyUiLocale + '.properties',
                                                        //success: function(r) {
                                                            //Ext.apply(NS.i18n, dhis2.util.parseJavaProperties(r.responseText));
                                                        //},
                                                        //failure: function() {
                                                            //console.log('No translations found for system locale (' + keyUiLocale + ')');
                                                        //},
                                                        //callback: function() {
                                                            //fn();
                                                        //}
                                                    //});
                                                //}
                                            //},
                                            //failure: function() {
                                                //Ext.Ajax.request({
                                                    //url: 'i18n/i18n_app_' + keyUiLocale + '.properties',
                                                    //success: function(r) {
                                                        //NS.i18n = dhis2.util.parseJavaProperties(r.responseText);
                                                    //},
                                                    //failure: function() {
                                                        //alert('No translations found for system locale (' + keyUiLocale + ') or default locale (' + defaultKeyUiLocale + ').');
                                                    //},
                                                    //callback: fn
                                                //});
                                            //}
                                        //});





        });
        });
        });
        });
    })();
});
