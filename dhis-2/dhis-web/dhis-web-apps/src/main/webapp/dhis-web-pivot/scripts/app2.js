Ext.onReady( function() {
	var N = PT;

    // initialize
    (function() {
        var appManager = N.AppManager,
            calendarManager = N.CalendarManager,
            requestManager = new N.Api.RequestManager(),
            manifestReq = $.getJSON('manifest.webapp'),
            systemInfoReq = $.getJSON('/api/system/info.json'),
            systemSettingsReq = $.getJSON('/api/systemSettings.json?key=keyCalendar&key=keyDateFormat&key=keyAnalysisRelativePeriod&key=keyHideUnapprovedDataInAnalytics'),
            userAccountReq = $.getJSON('/api/me/user-account.json');

        manifestReq.done(function(manifest) {
            appManager.manifest = manifest;

        systemInfoReq.done(function(systemInfo) {
            appManager.systemInfo = systemInfo;
            appManager.path = systemInfo.contextPath;

        systemSettingsReq.done(function(systemSettings) {
            appManager.systemSettings = systemSettings;

        userAccountReq.done(function(userAccount) {
            appManager.userAccount = userAccount;

            calendarManager.setBaseUrl(appManager.getPath());
            calendarManager.setDateFormat(appManager.getDateFormat());
            calendarManager.generate(appManager.systemSettings.keyCalendar);

        // requests
        (function() {
            var uiLocale = appManager.getUiLocale(),
                displayProperty = appManager.getDisplayProperty(),
                path = appManager.getPath();

            // i18n
            requestManager.add(new N.Api.Request({
                baseUrl: 'i18n/i18n_app.properties',
                type: 'ajax',
                success: function(r) {
                    var t = this;

                    N.I18nManager.add(dhis2.util.parseJavaProperties(r));

                    if (appManager.isUiLocaleDefault()) {
                        requestManager.ok(t);
                    }
                    else {
                        $.ajax({
                            url: 'i18n/i18n_app_' + uiLocale + '.properties',
                            success: function(r) {
                                N.I18nManager.add(dhis2.util.parseJavaProperties(r));
                            },
                            error: function() {
                                console.log('(i18n) No translations found for system locale (' + uiLocale + ')');
                            },
                            complete: function() {
                                requestManager.ok(t);
                            }
                        });
                    }
                },
                error: function() {
                    $.ajax({
                        url: 'i18n/i18n_app_' + uiLocale + '.properties',
                        success: function(r) {
                            N.I18nManager.add(dhis2.util.parseJavaProperties(r));
                        },
                        error: function() {
                            alert('(i18n) No translations found for system locale (' + uiLocale + ') or default locale (' + appManager.defaultUiLocale + ')');
                        },
                        complete: function() {
                            requestManager.ok(this);
                        }
                    });
                }
            }));

            // authorization
            requestManager.add(new N.Api.Request({
                baseUrl: path + '/api/me/authorization/F_VIEW_UNAPPROVED_DATA',
                success: function(r) {
                    appManager.viewUnapprovedData = r;
                    requestManager.ok(this);
                }
            }));

            // root nodes
            requestManager.add(new N.Api.Request({
                baseUrl: path + '/api/organisationUnits.json',
                params: [
                    'userDataViewFallback=true',
                    'fields=id,' + displayProperty + ',children[id,' + displayProperty + ']',
                    'paging=false'
                ],
                success: function(r) {
                    appManager.rootNodes = r.organisationUnits;
                    requestManager.ok(this);
                }
            }));

            // organisation unit levels
            requestManager.add(new N.Api.Request({
                baseUrl: path + '/api/organisationUnitLevels.json',
                params: [
                    'fields=id,' + displayProperty + 'level',
                    'paging=false'
                ],
                success: function(r) {
                    appManager.organisationUnitLevels = r.organisationUnitLevels;

                    if (!r.organisationUnitLevels.length) {
                        alert('No organisation unit levels found');
                    }

                    requestManager.ok(this);
                }
            }));

            // legend sets
            requestManager.add(new N.Api.Request({
                baseUrl: path + '/api/legendSets.json',
                params: [
                    'fields=id,' + displayProperty + ',legends[id,' + displayProperty + ',startValue,endValue,color]',
                    'paging=false'
                ],
                success: function(r) {
                    appManager.legendSets = r.legendSets;
                    requestManager.ok(this);
                }
            }));

            // dimensions
            requestManager.add(new N.Api.Request({
                baseUrl: path + '/api/dimensions.json',
                params: [
                    'fields=id,' + displayProperty,
                    'paging=false'
                ],
                success: function(r) {
                    appManager.dimensions = r.dimensions;
                    requestManager.ok(this);
                }
            }));

            // approval levels
            requestManager.add(new N.Api.Request({
                baseUrl: path + '/api/dataApprovalLevels.json',
                params: [
                    'order=level:asc',
                    'fields=id,' + displayProperty,
                    'paging=false'
                ],
                success: function(r) {
                    appManager.dataApprovalLevels = r.dataApprovalLevels;
                    requestManager.ok(this);
                }
            }));
        })();

        //requestManager.set(function() {

        requestManager.run();

        });
        });
        });
        });
    })();
});
