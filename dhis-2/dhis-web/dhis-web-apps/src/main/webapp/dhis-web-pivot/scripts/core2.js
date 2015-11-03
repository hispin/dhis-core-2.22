$( function() {
    var NS = PT = {};

    // convenience TODO import
    (function() {
        var isString,
            isNumber,
            isNumeric,
            isBoolean,
            isArray,
            isObject,
            isEmpty,
            isDefined,
            arrayFrom,
            arrayClean,
            arrayPluck,
            arrayIndexOf,
            arrayUnique,
            arrayContains,
            clone,
            enumerables = ['valueOf', 'toLocaleString', 'toString', 'constructor'];

        // enumerables
        (function() {
            for (i in { toString: 1 }) {
                enumerables = null;
            }
        })();

        isString = function(param) {
            return typeof param === 'string';
        };

        isNumber = function(param) {
            return typeof param === 'number' && isFinite(param);
        };

        isNumeric = function(param) {
            return !isNaN(parseFloat(param)) && isFinite(param);
        };

        isArray = ('isArray' in Array) ? Array.isArray : function(param) {
            return toString.call(param) === '[object Array]';
        };

        isObject = (toString.call(null) === '[object Object]') ? function(param) {
            return param !== null && param !== undefined && toString.call(param) === '[object Object]' && param.ownerDocument === undefined;
        } : function(param) {
            return toString.call(param) === '[object Object]';
        };

        isBoolean = function(param) {
            return typeof param === 'boolean';
        };

        isEmpty = function(array, allowEmptyString) {
            return (array == null) || (!allowEmptyString ? array === '' : false) || (isArray(array) && array.length === 0);
        };

        isDefined = function(param) {
            return typeof param !== 'undefined';
        };

        isPrimitive = function(param) {
            var type = typeof param;
            return type === 'string' || type === 'number' || type === 'boolean';
        };

        arrayFrom = function(param, isNewRef) {
            if (param === undefined || param === null) {
                return [];
            }

            if (Ext.isArray(param)) {
                return (isNewRef) ? slice.call(param) : param;
            }

            var type = typeof param;
            if (param && param.length !== undefined && type !== 'string' && (type !== 'function' || !param.apply)) {
                return ExtArray.toArray(param);
            }

            return [param];
        };

        arrayClean = function(array) {
            var results = [],
                i = 0,
                ln = array.length,
                item;

            for (; i < ln; i++) {
                item = array[i];

                if (!isEmpty(item)) {
                    results.push(item);
                }
            }

            return results;
        };

        arrayPluck = function(array, propertyName) {
            var newArray = [],
                i, len, item;

            for (i = 0, len = array.length; i < len; i++) {
                item = array[i];

                newArray.push(item[propertyName]);
            }

            return newArray;
        };

        arrayIndexOf = ('indexOf' in Array.prototype) ? function(array, item, from) {
            return Array.prototype.indexOf.call(array, item, from);
         } : function(array, item, from) {
            var i, len = array.length;

            for (i = (from < 0) ? Math.max(0, len + from) : from || 0; i < len; i++) {
                if (array[i] === item) {
                    return i;
                }
            }

            return -1;
        };

        // dep: arrayIndexOf
        arrayUnique = function(array) {
            var newArray = [],
                i = 0,
                len = array.length,
                item;

            for (; i < len; i++) {
                item = array[i];

                if (arrayIndexOf(newArray, item) === -1) {
                    newArray.push(item);
                }
            }

            return newArray;
        };

        // dep: arrayIndexOf
        arrayContains = ('indexOf' in Array.prototype) ? function(array, item) {
            return arrayPrototype.indexOf.call(array, item) !== -1;
        } : function(array, item) {
            var i, len;

            for (i = 0, len = array.length; i < len; i++) {
                if (array[i] === item) {
                    return true;
                }
            }

            return false;
        };

        clone = function(item) {
            if (item === null || item === undefined) {
                return item;
            }

            if (item.nodeType && item.cloneNode) {
                return item.cloneNode(true);
            }

            var type = toString.call(item),
                i, j, k, clone, key;

            if (type === '[object Date]') {
                return new Date(item.getTime());
            }

            if (type === '[object Array]') {
                i = item.length;

                clone = [];

                while (i--) {
                    clone[i] = Ext.clone(item[i]);
                }
            }
            else if (type === '[object Object]' && item.constructor === Object) {
                clone = {};

                for (key in item) {
                    clone[key] = Ext.clone(item[key]);
                }

                if (enumerables) {
                    for (j = enumerables.length; j--;) {
                        k = enumerables[j];
                        if (item.hasOwnProperty(k)) {
                            clone[k] = item[k];
                        }
                    }
                }
            }

            return clone || item;
        };

        NS.isString = isString;
        NS.isNumber = isNumber;
        NS.isNumeric = isNumeric;
        NS.isBoolean = isBoolean;
        NS.isArray = isArray;
        NS.isObject = isObject;
        NS.isEmpty = isEmpty;
        NS.isDefined = isDefined;
        NS.arrayFrom = arrayFrom;
        NS.arrayClean = arrayClean;
        NS.arrayPluck = arrayPluck;
        NS.arrayIndexOf = arrayIndexOf;
        NS.arrayUnique = arrayUnique;
        NS.arrayContains = arrayContains;
        NS.clone = clone;
    })();

    // date manager TODO import
    (function() {
        var DateManager = function() {};

        DateManager.prototype.getYYYYMMDD = function(param) {
            if (!(Object.prototype.toString.call(param) === '[object Date]' && param.toString() !== 'Invalid date')) {
                return null;
            }

            var date = new Date(param),
                month = '' + (1 + date.getMonth()),
                day = '' + date.getDate();

            month = month.length === 1 ? '0' + month : month;
            day = day.length === 1 ? '0' + day : day;

            return date.getFullYear() + '-' + month + '-' + day;
        };

        NS.DateManager = new DateManager();
    })();

    // NS I18n
    (function() {
        var I18n = function(config) {
            this.map = config || {};
        };

        I18n.prototype.get = function(key) {
            return this.map[key];
        };

        I18n.prototype.add = function(obj) {
            $.extend(this.map, obj);
        };

        NS.I18n = new I18n();
    })();

    // NS conf
    (function() {
        var conf = function() {
            var t = this;

            this.finals = {
				dimension: {
					data: {
						value: 'data',
						name: NS.I18n.data || 'Data',
						dimensionName: 'dx',
						objectName: 'dx'
					},
					category: {
						name: NS.I18n.assigned_categories || 'Assigned categories',
						dimensionName: 'co',
						objectName: 'co',
					},
					indicator: {
						value: 'indicators',
						name: NS.I18n.indicators || 'Indicators',
						dimensionName: 'dx',
						objectName: 'in'
					},
					dataElement: {
						value: 'dataElements',
						name: NS.I18n.data_elements || 'Data elements',
						dimensionName: 'dx',
						objectName: 'de'
					},
					operand: {
						value: 'operand',
						name: 'Operand',
						dimensionName: 'dx',
						objectName: 'dc'
					},
					dataSet: {
						value: 'dataSets',
						name: NS.I18n.data_sets || 'Data sets',
						dimensionName: 'dx',
						objectName: 'ds'
					},
					eventDataItem: {
						value: 'eventDataItem',
						name: NS.I18n.event_data_items || 'Event data items',
						dimensionName: 'dx',
						objectName: 'di'
					},
					programIndicator: {
						value: 'programIndicator',
						name: NS.I18n.program_indicators || 'Program indicators',
						dimensionName: 'dx',
						objectName: 'pi'
					},
					period: {
						value: 'period',
						name: NS.I18n.periods || 'Periods',
						dimensionName: 'pe',
						objectName: 'pe'
					},
					fixedPeriod: {
						value: 'periods'
					},
					relativePeriod: {
						value: 'relativePeriods'
					},
					organisationUnit: {
						value: 'organisationUnits',
						name: NS.I18n.organisation_units || 'Organisation units',
						dimensionName: 'ou',
						objectName: 'ou'
					},
					dimension: {
						value: 'dimension'
						//objectName: 'di'
					},
					value: {
						value: 'value'
					}
				},
				root: {
					id: 'root'
				},
                style: {
                    'normal': 'NORMAL',
                    'compact': 'COMPACT',
                    'xcompact': 'XCOMPACT',
                    'comfortable': 'COMFORTABLE',
                    'xcomfortable': 'XCOMFORTABLE',
                    'small': 'SMALL',
                    'xsmall': 'XSMALL',
                    'large': 'LARGE',
                    'xlarge': 'XLARGE',
                    'space': 'SPACE',
                    'comma': 'COMMA',
                    'none': 'NONE',
                    'default_': 'DEFAULT'
                }
			};

            (function() {
                var dimConf = t.finals.dimension;

                dimConf.objectNameMap = {};
                dimConf.objectNameMap[dimConf.data.objectName] = dimConf.data;
                dimConf.objectNameMap[dimConf.indicator.objectName] = dimConf.indicator;
                dimConf.objectNameMap[dimConf.dataElement.objectName] = dimConf.dataElement;
                dimConf.objectNameMap[dimConf.operand.objectName] = dimConf.operand;
                dimConf.objectNameMap[dimConf.dataSet.objectName] = dimConf.dataSet;
                dimConf.objectNameMap[dimConf.category.objectName] = dimConf.category;
                dimConf.objectNameMap[dimConf.period.objectName] = dimConf.period;
                dimConf.objectNameMap[dimConf.organisationUnit.objectName] = dimConf.organisationUnit;
                dimConf.objectNameMap[dimConf.dimension.objectName] = dimConf.dimension;
            })();

            this.period = {
				periodTypes: [
					{id: 'Daily', name: NS.I18n.daily},
					{id: 'Weekly', name: NS.I18n.weekly},
					{id: 'Monthly', name: NS.I18n.monthly},
					{id: 'BiMonthly', name: NS.I18n.bimonthly},
					{id: 'Quarterly', name: NS.I18n.quarterly},
					{id: 'SixMonthly', name: NS.I18n.sixmonthly},
					{id: 'SixMonthlyApril', name: NS.I18n.sixmonthly_april},
					{id: 'Yearly', name: NS.I18n.yearly},
					{id: 'FinancialOct', name: NS.I18n.financial_oct},
					{id: 'FinancialJuly', name: NS.I18n.financial_july},
					{id: 'FinancialApril', name: NS.I18n.financial_april}
				],
                relativePeriods: []
			};

            this.valueType = {
            	numericTypes: ['NUMBER','UNIT_INTERVAL','PERCENTAGE','INTEGER','INTEGER_POSITIVE','INTEGER_NEGATIVE','INTEGER_ZERO_OR_POSITIVE'],
            	textTypes: ['TEXT','LONG_TEXT','LETTER','PHONE_NUMBER','EMAIL'],
            	booleanTypes: ['BOOLEAN','TRUE_ONLY'],
            	dateTypes: ['DATE','DATETIME'],
            	aggregateTypes: ['NUMBER','UNIT_INTERVAL','PERCENTAGE','INTEGER','INTEGER_POSITIVE','INTEGER_NEGATIVE','INTEGER_ZERO_OR_POSITIVE','BOOLEAN','TRUE_ONLY']
            };

			this.layout = {
				west_width: 424,
				west_fieldset_width: 420,
				west_width_padding: 2,
				west_fill: 2,
				west_fill_accordion_indicator: 81,
				west_fill_accordion_dataelement: 81,
				west_fill_accordion_dataset: 56,
                west_fill_accordion_eventdataitem: 81,
                west_fill_accordion_programindicator: 81,
				west_fill_accordion_period: 310,
				west_fill_accordion_organisationunit: 58,
                west_fill_accordion_group: 31,
				west_maxheight_accordion_indicator: 400,
				west_maxheight_accordion_dataelement: 400,
				west_maxheight_accordion_dataset: 400,
				west_maxheight_accordion_period: 513,
				west_maxheight_accordion_organisationunit: 900,
				west_maxheight_accordion_group: 340,
				west_maxheight_accordion_options: 449,
				west_scrollbarheight_accordion_indicator: 300,
				west_scrollbarheight_accordion_dataelement: 300,
				west_scrollbarheight_accordion_dataset: 300,
				west_scrollbarheight_accordion_period: 450,
				west_scrollbarheight_accordion_organisationunit: 450,
				west_scrollbarheight_accordion_group: 300,
				east_tbar_height: 31,
				east_gridcolumn_height: 30,
				form_label_width: 55,
				window_favorite_ypos: 100,
				window_confirm_width: 250,
				window_share_width: 500,
				grid_favorite_width: 420,
				grid_row_height: 27,
				treepanel_minheight: 135,
				treepanel_maxheight: 400,
				treepanel_fill_default: 310,
				treepanel_toolbar_menu_width_group: 140,
				treepanel_toolbar_menu_width_level: 120,
				multiselect_minheight: 100,
				multiselect_maxheight: 250,
				multiselect_fill_default: 345,
				multiselect_fill_reportingrates: 315
			};

			this.style = {
				displayDensity: {},
				fontSize: {},
				digitGroupSeparator: {}
            };

            (function() {
                var map = t.finals.style,
                    displayDensity = t.style.displayDensity,
                    fontSize = t.style.fontSize,
                    digitGroupSeparator = t.style.digitGroupSeparator;

                displayDensity[map.xcompact] = '2px';
                displayDensity[map.compact] = '4px';
                displayDensity[map.normal] = '6px';
                displayDensity[map.comfortable] = '8px';
                displayDensity[map.xcomfortable] = '10px';

                fontSize[map.xsmall] = '9px';
                fontSize[map.small] = '10px';
                fontSize[map.normal] = '11px';
                fontSize[map.large] = '12px';
                fontSize[map.xlarge] = '14px';

                digitGroupSeparator[map.space] = '&nbsp;';
                digitGroupSeparator[map.comma] = ',';
                digitGroupSeparator[map.none] = '';
            })();

            this.url = {
                analysisFields: [
                    '*',
                    'program[id,name]',
                    'programStage[id,name]',
                    'columns[dimension,filter,items[id,' + init.namePropertyUrl + ']]',
                    'rows[dimension,filter,items[id,' + init.namePropertyUrl + ']]',
                    'filters[dimension,filter,items[id,' + init.namePropertyUrl + ']]',
                    '!lastUpdated',
                    '!href',
                    '!created',
                    '!publicAccess',
                    '!rewindRelativePeriods',
                    '!userOrganisationUnit',
                    '!userOrganisationUnitChildren',
                    '!userOrganisationUnitGrandChildren',
                    '!externalAccess',
                    '!access',
                    '!relativePeriods',
                    '!columnDimensions',
                    '!rowDimensions',
                    '!filterDimensions',
                    '!user',
                    '!organisationUnitGroups',
                    '!itemOrganisationUnitGroups',
                    '!userGroupAccesses',
                    '!indicators',
                    '!dataElements',
                    '!dataElementOperands',
                    '!dataElementGroups',
                    '!dataSets',
                    '!periods',
                    '!organisationUnitLevels',
                    '!organisationUnits'
                ]
            };
        };

        NS.conf = new conf();
    })();

    // Api
    (function() {
        NS.Api = {};

        // Record
        (function() {
            var Record = NS.Api.Record = function(config) {
                var t = this;

                config = NS.isObject(config) ? config : {};

                // constructor
                t.id = config.id;
                t.name = config.name;
            };

            Record.prototype.log = function(text, noError) {
                if (!noError) {
                    console.log(text, this);
                }
            };

            Record.prototype.val = function(noError) {
                if (!NS.isString(this.id)) {
                    this.log('(Record) Id is not a string', noError);
                    return null;
                }

                return this;
            };
        })();

        // Dimension
        (function() {
            var Dimension = NS.Api.Dimension = function(config) {
                var t = this,
                    items = [];

                config = NS.isObject(config) ? config : {};
                config.items = NS.arrayFrom(config.items);

                // constructor
                t.dimension = config.dimension;

                t.items = config.items.map(function(record) {
                    return (new NS.Api.Record(record)).val();
                });
            };

            Dimension.prototype.log = function(text, noError) {
                if (!noError) {
                    console.log(text, this);
                }
            };

            Dimension.prototype.val = function(noError) {
                if (!NS.isString(this.dimension)) {
                    this.log('(Dimension) Dimension is not a string', noError);
                    return null;
                }

                if (!this.items.length && this.dimension !== 'co') {
                    this.log('(Dimension) No items', noError);
                    return null;
                }

                return this;
            };

            Dimension.prototype.getRecords = function(sortProperty) {
                sortProperty = NS.arrayContains(['id', 'name'], sortProperty) ? sortProperty : null;

                return sortProperty ? this.items.sort(function(a, b) { return a[sortProperty] > b[sortProperty];}) : this.items;
            };

            // dep 1

            Dimension.prototype.getRecordIds = function(isSorted) {
                return NS.arrayPluck(this.getRecords(isSorted ? 'id' : null), 'id');
            };

            Dimension.prototype.getRecordNames = function(isSorted) {
                return NS.arrayPluck(this.getRecords(isSorted ? 'name' : null), 'name');
            };

            Dimension.prototype.url = function() {
                return 'dimension=' + this.dimension + ':' + NS.arrayUnique(this.getRecordIds()).join(';');
            };
        })();

        // Axis
        (function() {
            var Axis = NS.Api.Axis = function(config) {
                var t = [];

                // constructor
                config = NS.arrayFrom(config);

                (function() {
                    for (var i = 0; i < config.length; i++) {
                        t.push((new NS.Api.Dimension(config[i])).val());
                    }
                })();

                // prototype
                t.log = function(text, noError) {
                    if (!noError) {
                        console.log(text, this);
                    }
                };

                t.val = function(noError) {
                    if (!this.length) {
                        this.log('(Axis) No dimensions', noError);
                        return null;
                    }

                    return this;
                };

                t.has = function(dimensionName) {
                    return this.some(function(dimension) {
                        return dimension.dimension === dimensionName;
                    });
                };

                t.sorted = function() {
                    return NS.clone(this).sort(function(a, b) {
                        return a.dimension - b.dimension;
                    });
                };

                return t;
            };
        })();

        // Layout
        (function() {
            var Layout = NS.Api.Layout = function(config, applyConfig, forceApplyConfig) {
                var t = this;

                config = NS.isObject(config) ? config : {};
                $.extend(config, applyConfig);

                // constructor
                t.columns = (NS.Api.Axis(config.columns)).val();
                t.rows = (NS.Api.Axis(config.rows)).val();
                t.filters = (NS.Api.Axis(config.filters)).val();

                t.showColTotals = NS.isBoolean(config.colTotals) ? config.colTotals : (NS.isBoolean(config.showColTotals) ? config.showColTotals : true);
                t.showRowTotals = NS.isBoolean(config.rowTotals) ? config.rowTotals : (NS.isBoolean(config.showRowTotals) ? config.showRowTotals : true);
                t.showColSubTotals = NS.isBoolean(config.colSubTotals) ? config.colSubTotals : (NS.isBoolean(config.showColSubTotals) ? config.showColSubTotals : true);
                t.showRowSubTotals = NS.isBoolean(config.rowSubTotals) ? config.rowSubTotals : (NS.isBoolean(config.showRowSubTotals) ? config.showRowSubTotals : true);
                t.showDimensionLabels = NS.isBoolean(config.showDimensionLabels) ? config.showDimensionLabels : (NS.isBoolean(config.showDimensionLabels) ? config.showDimensionLabels : true);
                t.hideEmptyRows = NS.isBoolean(config.hideEmptyRows) ? config.hideEmptyRows : false;
                t.skipRounding = NS.isBoolean(config.skipRounding) ? config.skipRounding : false;
                t.aggregationType = NS.isString(config.aggregationType) ? config.aggregationType : NS.conf.finals.style.default_;
                t.dataApprovalLevel = NS.isObject(config.dataApprovalLevel) && NS.isString(config.dataApprovalLevel.id) ? config.dataApprovalLevel : null;
                t.showHierarchy = NS.isBoolean(config.showHierarchy) ? config.showHierarchy : false;
                t.completedOnly = NS.isBoolean(config.completedOnly) ? config.completedOnly : false;
                t.displayDensity = NS.isString(config.displayDensity) && !NS.isEmpty(config.displayDensity) ? config.displayDensity : NS.conf.finals.style.normal;
                t.fontSize = NS.isString(config.fontSize) && !NS.isEmpty(config.fontSize) ? config.fontSize : NS.conf.finals.style.normal;
                t.digitGroupSeparator = NS.isString(config.digitGroupSeparator) && !NS.isEmpty(config.digitGroupSeparator) ? config.digitGroupSeparator : NS.conf.finals.style.space;

                t.legendSet = (new NS.Api.Record(config.legendSet)).val(true);

                t.parentGraphMap = NS.isObject(config.parentGraphMap) ? config.parentGraphMap : null;

                if (Ext.isObject(config.program)) {
                    t.program = config.program;
                }

                    // report table
                t.reportingPeriod = NS.isObject(config.reportParams) && NS.isBoolean(config.reportParams.paramReportingPeriod) ? config.reportParams.paramReportingPeriod : (NS.isBoolean(config.reportingPeriod) ? config.reportingPeriod : false);
                t.organisationUnit =  NS.isObject(config.reportParams) && NS.isBoolean(config.reportParams.paramOrganisationUnit) ? config.reportParams.paramOrganisationUnit : (NS.isBoolean(config.organisationUnit) ? config.organisationUnit : false);
                t.parentOrganisationUnit = NS.isObject(config.reportParams) && NS.isBoolean(config.reportParams.paramParentOrganisationUnit) ? config.reportParams.paramParentOrganisationUnit : (NS.isBoolean(config.parentOrganisationUnit) ? config.parentOrganisationUnit : false);

                t.regression = NS.isBoolean(config.regression) ? config.regression : false;
                t.cumulative = NS.isBoolean(config.cumulative) ? config.cumulative : false;
                t.sortOrder = NS.isNumber(config.sortOrder) ? config.sortOrder : 0;
                t.topLimit = NS.isNumber(config.topLimit) ? config.topLimit : 0;

                    // non model

                    // id
                if (NS.isString(config.id)) {
                    t.id = config.id;
                }

                    // name
                if (NS.isString(config.name)) {
                    t.name = config.name;
                }

                    // sorting
                if (NS.isObject(config.sorting) && NS.isDefined(config.sorting.id) && NS.isString(config.sorting.direction)) {
                    t.sorting = config.sorting;
                }

                    // displayProperty
                if (NS.isString(config.displayProperty)) {
                    t.displayProperty = config.displayProperty;
                }

                    // userOrgUnit
                if (NS.arrayFrom(config.userOrgUnit).length) {
                    t.userOrgUnit = NS.arrayFrom(config.userOrgUnit);
                }

                    // relative period date
                if (NS.DateManager.getYYYYMMDD(config.relativePeriodDate)) {
                    t.relativePeriodDate = NS.DateManager.getYYYYMMDD(config.relativePeriodDate);
                }

                $.extend(t, forceApplyConfig);

                // uninitialized
                t.dimensionNameIdsMap;
            };

            Layout.prototype.log = function(text, noError) {
                if (!noError) {
                    console.log(text, this);
                }
            };

            Layout.prototype.alert = function(text, noError) {
                if (!noError) {
                    alert(text);
                }
            };

            Layout.prototype.getAxes = function(includeFilter) {
                return NS.arrayClean([this.columns, this.rows, (includeFilter ? this.filters : null)]);
            };

            Layout.prototype.getUserOrgUnitUrl = function() {
                if (NS.isArray(this.userOrgUnit) && this.userOrgUnit.length) {
                    return 'userOrgUnit=' + this.userOrgUnit.join(';');
                }
            };

            // dep 1

            Layout.prototype.hasDimension = function(dimensionName, includeFilter) {
                var axes = this.getAxes(includeFilter);

                return axes.some(function(axis) {
                    return axis.has(dimensionName);
                });
            };

            Layout.prototype.getDimensions = function(includeFilter, isSorted) {
                var axes = this.getAxes(includeFilter),
                    dimensions = [];

                axes.forEach(function(axis) {
                    dimensions = dimensions.concat(axis);
                });

                return isSorted ? dimensions.sort(function(a, b) {return a.dimension > b.dimension;}) : dimensions;
            };

            Layout.prototype.getDimensionNameIdsMap = function() {
                if (this.dimensionNameIdsMap) {
                    return this.dimensionNameIdsMap;
                }

                var dimensions = this.getDimensions(true),
                    map = {};

                dimensions.forEach(function(dimension) {
                    map[dimension.dimension] = dimension.getRecordIds();
                });

                return this.dimensionNameIdsMap = map;
            };

            // dep 2

            Layout.prototype.getDimensionNames = function(includeFilter, isSorted) {
                var names = NS.arrayPluck(this.getDimensions(includeFilter), 'dimension');

                return isSorted ? names.sort() : names;
            };

            Layout.prototype.val = function(noError) {
                var dimConf = NS.conf.finals.dimension;

                if (!(this.columns || this.rows)) {
                    this.alert(NS.I18n.get('at_least_one_dimension_must_be_specified_as_row_or_column'), noError); //todo alert
                    return null;
                }

                if (!this.hasDimension(dimConf.period.dimensionName)) {
                    this.alert(NS.I18n.get('at_least_one_period_must_be_specified_as_column_row_or_filter'), noError); //todo alert
                    return null;
                }

                return this;
            };

            Layout.prototype.url = function(isSorted) {
                var aggTypes = ['COUNT', 'SUM', 'STDDEV', 'VARIANCE', 'MIN', 'MAX'],
                    displayProperty = this.displayProperty || 'name',
                    request = new NS.Api.Request(),
                    i;

                // dimensions
                for (i = 0, axisDimensions = this.getDimensions(false, isSorted), dimension; i < axisDimensions.length; i++) {
                    dimension = axisDimensions.length;

                    request.add(dimension.url(isSorted));
                }

                // filters
                if (this.filters) {
                    this.filters.each(function(dimension) {
                        request.add(dimension.url(isSorted));
                    });
                }

                // hierarchy
                if (this.showHierarchy) {
                    request.add('hierarchyMeta=true');
                }

                // completed only
                if (this.completedOnly) {
                    request.add('completedOnly=true');
                }

                // aggregation type
                if (NS.arrayContains(aggTypes, this.aggregationType)) {
                    request.add('aggregationType=' + this.aggregationType);
                }

                // user org unit
                if (NS.isArray(this.userOrgUnit) && this.userOrgUnit.length) {
                    request.add(this.getUserOrgUnitUrl());
                }

                // data approval level
                if (NS.isObject(this.dataApprovalLevel) && NS.isString(this.dataApprovalLevel.id) && this.dataApprovalLevel.id !== 'DEFAULT') {
                    request.add('approvalLevel=' + this.dataApprovalLevel.id);
                }

                // TODO program
                if (NS.isObject(this.program) && NS.isString(this.program.id)) {
                    request.add('program=' + this.program.id);
                }

                // relative period date
                if (this.relativePeriodDate) {
                    request.add('relativePeriodDate=' + this.relativePeriodDate);
                }

                // skip rounding
                if (this.skipRounding) {
                    request.add('skipRounding=true');
                }

                // display property
                request.add('displayProperty=' + ((this.displayProperty || '').toUpperCase()));

                return request.url();
            };


            //Layout.prototype.data = function() {


        })();

        // Request
        (function() {
            var Request = NS.Api.Request = function(config) {
                var t = this;

                // constructor
                t.params = [];

                (function() {
                    if (NS.isArray(config) && config.length) {
                        t.params = config;
                    }
                })();
            };

            Request.prototype.add = function(param) {
                if (NS.isString(param)) {
                    this.params.push(param);
                }
            };

            Request.prototype.url = function() {
                return '?' + this.params.join('&').replace(/#/g, '.');
            };
        })();

        // Header
        (function() {
            var Header = NS.Api.Header = function(config) {
                var t = this;

                config = NS.isObject(config) ? config : {};

                // constructor
                $.extend(this, config);

                // uninitialized
                t.index;
            };

            Header.prototype.setIndex = function(index) {
                if (NS.isNumeric(index)) {
                    this.index = parseInt(index);
                }
            };
        })();

        // Response
        (function() {
            var Response = NS.Api.Response = function(config) {
                var t = this;

                config = NS.isObject(config) ? config : {};

                t.headers = config.headers;
                t.metaData = config.metaData;
                t.rows = config.rows;

                // transient
                t.nameHeaderMap = function() {
                    for (var i = 0, map = {}; i < t.headers.length; i++) {
                        map[t.headers[i].name] = t.headers[i];
                    }

                    return map;
                }();

                // uninitialized
                t.idValueMap = {};

                // Header: index
                (function() {
                    for (var i = 0, header; i < t.headers.length; i++) {
                        t.headers[i].setIndex(i);
                    }
                })();
            };

            Response.prototype.getHeaderByName = function(name) {
                return this.nameHeaderMap[name];
            };

            Response.prototype.getHeaderIndexByName = function(name) {
                return this.nameHeaderMap[name].index;
            };

            Response.prototype.getNameById = function(id) {
                return this.metaData.names[id];
            };
        })();
    })();





});
