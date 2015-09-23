/* global trackerCapture, angular */

trackerCapture.controller('TEIAddController', 
    function($scope, 
            $rootScope,
            $translate,
            $timeout,
            DateUtils,
            CurrentSelection,
            OperatorFactory,
            AttributesFactory,
            EntityQueryFactory,
            OrgUnitFactory,
            TEIService,
            TEIGridService,
            DialogService,
            Paginator,
            SessionStorageService,
            $modalInstance, 
            relationshipTypes,
            selectedProgram,
            relatedProgramRelationship,
            selections,
            selectedAttribute,
            addingRelationship,
            selectedTei){
    
    $scope.relationshipTypes = relationshipTypes;
    $scope.addingRelationship = addingRelationship;
    $scope.selectedAttribute = selectedAttribute;
    $scope.selectedProgram = selectedProgram;
    $scope.relatedProgramRelationship = relatedProgramRelationship;
    $scope.selectedTei = selectedTei;
    $scope.programs = selections.prs;
    $scope.attributesById = CurrentSelection.getAttributesById();
    
    if($scope.addingRelationship){
        $scope.teiAddLabel = $translate.instant('add_relationship');
    }
    else{        
        $scope.teiAddLabel = $scope.selectedAttribute && $scope.selectedAttribute.name ? $scope.selectedAttribute.name : $translate.instant('tracker_associate');
    }
    
    $scope.searchOuTree = false;
    $scope.orgUnitLabel = $translate.instant('org_unit');
    
    $scope.selectedRelationship = {};
    $scope.relationship = {};
    
    var invalidTeis = [];
    invalidTeis.push($scope.selectedTei.trackedEntityInstance);
    if($scope.selectedTei.relationships){
        angular.forEach($scope.selectedTei.relationships, function(rel){
            invalidTeis.push(rel.trackedEntityInstanceB);
        });
    }    

    //Selections
    $scope.selectedOrgUnit = SessionStorageService.get('SELECTED_OU');
    $scope.optionSets = selections.optionSets;
    $scope.selectedTeiForDisplay = angular.copy($scope.selectedTei);
    $scope.ouModes = [{name: 'SELECTED'}, {name: 'CHILDREN'}, {name: 'DESCENDANTS'}, {name: 'ACCESSIBLE'}];         
    $scope.selectedOuMode = $scope.ouModes[0];
    
    //Paging
    $scope.pager = {pageSize: 50, page: 1, toolBarDisplay: 5}; 
    
    //Searching
    $scope.showAdvancedSearchDiv = false;
    $scope.searchText = {value: null};
    $scope.emptySearchText = false;
    $scope.searchFilterExists = false;   
    $scope.defaultOperators = OperatorFactory.defaultOperators;
    $scope.boolOperators = OperatorFactory.boolOperators;
    
    $scope.trackedEntityList = null; 
    $scope.enrollment = {programStartDate: '', programEndDate: '', operator: $scope.defaultOperators[0]};
   
    $scope.searchMode = {listAll: 'LIST_ALL', freeText: 'FREE_TEXT', attributeBased: 'ATTRIBUTE_BASED'};
    $scope.selectedSearchMode = $scope.searchMode.listAll;
    
    if(angular.isObject($scope.programs) && $scope.programs.length === 1){
        $scope.selectedProgramForRelative = $scope.programs[0];        
    } 
    
    if($scope.selectedProgram){
        if($scope.selectedProgram.relatedProgram && $scope.relatedProgramRelationship){
            angular.forEach($scope.programs, function(pr){
                if(pr.id === $scope.selectedProgram.relatedProgram.id){
                    $scope.selectedProgramForRelative = pr;
                }
            });
        }
        
        if($scope.selectedProgram.relationshipType){
            angular.forEach($scope.relationshipTypes, function(rel){
                if(rel.id === $scope.selectedProgram.relationshipType.id){
                    $scope.relationship.selected = rel;
                }
            });
        }
    }
    
    //watch for selection of relationship
    $scope.$watch('relationship.selected', function() {        
        if( angular.isObject($scope.relationship.selected)){
            $scope.selectedRelationship = {aIsToB: $scope.relationship.selected.aIsToB, bIsToA: $scope.relationship.selected.bIsToA};  
        }
    });
    
    function resetFields(){
        
        $scope.teiForRelationship = null;
        $scope.teiFetched = false;    
        $scope.emptySearchText = false;
        $scope.emptySearchAttribute = false;
        $scope.showAdvancedSearchDiv = false;
        $scope.showRegistrationDiv = false;  
        $scope.showTrackedEntityDiv = false;
        $scope.trackedEntityList = null; 
        $scope.teiCount = null;

        $scope.queryUrl = null;
        $scope.programUrl = null;
        $scope.attributeUrl = {url: null, hasValue: false};
        $scope.sortColumn = {};
    }
    
    //listen for selections
    $scope.$on('relationship', function(event, args){
        if(args.result === 'SUCCESS'){
            var relationshipInfo = CurrentSelection.getRelationshipInfo();
            $scope.teiForRelationship = relationshipInfo.tei;
        }
        
        if(args.result === 'CANCEL'){
            $scope.showRegistration();
        }        
    });
    
    //sortGrid
    $scope.sortGrid = function(gridHeader){
        if ($scope.sortColumn && $scope.sortColumn.id === gridHeader.id){
            $scope.reverse = !$scope.reverse;
            return;
        }        
        $scope.sortColumn = gridHeader;
        if($scope.sortColumn.valueType === 'date'){
            $scope.reverse = true;
        }
        else{
            $scope.reverse = false;    
        }
    };
    
    $scope.d2Sort = function(tei){        
        if($scope.sortColumn && $scope.sortColumn.valueType === 'date'){            
            var d = tei[$scope.sortColumn.id];         
            return DateUtils.getDate(d);
        }
        return tei[$scope.sortColumn.id];
    };
    
    $scope.search = function(mode){ 
        
        resetFields();
        
        $scope.selectedSearchMode = mode;        
   
        if($scope.selectedProgramForRelative){
            $scope.programUrl = 'program=' + $scope.selectedProgramForRelative.id;
        }        
        
        //check search mode
        if( $scope.selectedSearchMode === $scope.searchMode.freeText ){ 
            
            if(!$scope.searchText.value){                
                $scope.emptySearchText = true;
                $scope.teiFetched = false;   
                $scope.teiCount = null;
                return;
            }       

            $scope.queryUrl = 'query=LIKE:' + $scope.searchText.value;                     
        }
        
        if( $scope.selectedSearchMode === $scope.searchMode.attributeBased ){            
            $scope.searchText.value = null;
            $scope.attributeUrl = EntityQueryFactory.getAttributesQuery($scope.attributes, $scope.enrollment);
            
            if(!$scope.attributeUrl.hasValue && !$scope.selectedProgramForRelative){
                $scope.emptySearchAttribute = true;
                $scope.teiFetched = false;   
                $scope.teiCount = null;
                return;
            }
        }
        
        $scope.fetchTei();
    };
    
    $scope.fetchTei = function(){

        //get events for the specified parameters
        TEIService.search($scope.selectedOrgUnit.id, 
                                            $scope.selectedOuMode.name,
                                            $scope.queryUrl,
                                            $scope.programUrl,
                                            $scope.attributeUrl.url,
                                            $scope.pager,
                                            true).then(function(data){
            //$scope.trackedEntityList = data;            
            if(data.rows){
                $scope.teiCount = data.rows.length;
            }                    
            
            if( data.metaData.pager ){
                $scope.pager = data.metaData.pager;
                $scope.pager.toolBarDisplay = 5;

                Paginator.setPage($scope.pager.page);
                Paginator.setPageCount($scope.pager.pageCount);
                Paginator.setPageSize($scope.pager.pageSize);
                Paginator.setItemCount($scope.pager.total);                    
            }
            
            //process tei grid
            $scope.trackedEntityList = TEIGridService.format(data,false, $scope.optionSets, invalidTeis);
            $scope.showTrackedEntityDiv = true;
            $scope.teiFetched = true;
            
            if(!$scope.sortColumn.id){                                      
                $scope.sortGrid({id: 'created', name: $translate('registration_date'), valueType: 'date', displayInListNoProgram: false, showFilter: false, show: false});
            }
            
        });
    };
    
    //set attributes as per selected program
    $scope.setAttributesForSearch = function(program){
        
        $scope.selectedProgramForRelative = program;
        AttributesFactory.getByProgram($scope.selectedProgramForRelative).then(function(atts){
            $scope.attributes = atts;
            $scope.attributes = $scope.generateAttributeFilters($scope.attributes);
            $scope.gridColumns = $scope.generateGridColumns($scope.attributes);
        });
        
        $scope.search( $scope.selectedSearchMode );        
    }; 
    
    $scope.setAttributesForSearch( $scope.selectedProgramForRelative );
    
    $scope.jumpToPage = function(){
        if($scope.pager && $scope.pager.page && $scope.pager.pageCount && $scope.pager.page > $scope.pager.pageCount){
            $scope.pager.page = $scope.pager.pageCount;
        }
        
        $scope.search($scope.selectedSearchMode);
    };
    
    $scope.resetPageSize = function(){
        $scope.pager.page = 1;        
        $scope.search($scope.selectedSearchMode);
    };
    
    $scope.getPage = function(page){
        $scope.pager.page = page;
        $scope.search($scope.selectedSearchMode);
    };
    
    $scope.generateAttributeFilters = function(attributes){

        angular.forEach(attributes, function(attribute){
            if(attribute.valueType === 'number' || attribute.valueType === 'date'){
                attribute.operator = $scope.defaultOperators[0];
            }
        });
                    
        return attributes;
    };

    //generate grid columns from teilist attributes
    $scope.generateGridColumns = function(attributes){

        var columns = attributes ? angular.copy(attributes) : [];
       
        //also add extra columns which are not part of attributes (orgunit for example)
        columns.push({id: 'orgUnitName', name: 'Organisation unit', type: 'TEXT', displayInListNoProgram: false});
        columns.push({id: 'created', name: 'Registration date', type: 'TEXT', displayInListNoProgram: false});
        
        //generate grid column for the selected program/attributes
        angular.forEach(columns, function(column){
            if(column.id === 'orgUnitName' && $scope.selectedOuMode.name !== 'SELECTED'){
                column.show = true;
            }
            
            if(column.displayInListNoProgram){
                column.show = true;
            }           
           
            if(column.type === 'date'){
                $scope.filterText[column.id]= {start: '', end: ''};
            }
        });        
        return columns;        
    };   
    
    $scope.showHideSearch = function(simpleSearch){
        if(simpleSearch){
            $scope.showAdvancedSearchDiv = false;
        }
        else{
            $scope.showAdvancedSearchDiv = !$scope.showAdvancedSearchDiv;
        }
        
        if($scope.showAdvancedSearchDiv){
            $scope.showTrackedEntityDiv = false;
        }
        else{
            $scope.showTrackedEntityDiv = true;
        }
    };
    
    $scope.showRegistration = function(){
        $scope.showRegistrationDiv = !$scope.showRegistrationDiv;
        
        if($scope.showRegistrationDiv){
            $scope.showTrackedEntityDiv = false;
            $timeout(function() {
                var mainTei = angular.copy($scope.selectedTei); 
                angular.forEach(mainTei.attributes, function(att){
                    mainTei[att.attribute] = att.value;
                });
                $rootScope.$broadcast('registrationWidget', {registrationMode: 'RELATIONSHIP', mainTei: mainTei, relativeProgram: $scope.selectedProgramForRelative});
            }, 200);
        }
        else{
            $scope.showTrackedEntityDiv = true;            
        }        
    };
    
    $scope.close = function () {
        $modalInstance.close($scope.selectedTei.relationships ? $scope.selectedTei.relationships : []);
        $rootScope.showAddRelationshipDiv = !$rootScope.showAddRelationshipDiv;
    };
    
    $scope.setRelationshipSides = function(side){
        if(side === 'A'){            
            $scope.selectedRelationship.bIsToA = $scope.selectedRelationship.aIsToB === $scope.relationship.selected.aIsToB ? $scope.relationship.selected.bIsToA : $scope.relationship.selected.aIsToB;
        }
        if(side === 'B'){
            $scope.selectedRelationship.aIsToB = $scope.selectedRelationship.bIsToA === $scope.relationship.selected.bIsToA ? $scope.relationship.selected.aIsToB : $scope.relationship.selected.bIsToA;
        }
    };
    
    $scope.assignRelationship = function(relativeTei){
        $scope.teiForRelationship = relativeTei;
        $rootScope.showAddRelationshipDiv = !$rootScope.showAddRelationshipDiv;
    };
    
    
    $scope.back = function(){
        $scope.teiForRelationship = null;
        $rootScope.showAddRelationshipDiv = !$rootScope.showAddRelationshipDiv;
    };
    
    $scope.addRelationship = function(){
        if($scope.selectedTei && $scope.teiForRelationship && $scope.relationship.selected){            
            var tei = angular.copy($scope.selectedTei);
            var relationship = {};
            relationship.relationship = $scope.relationship.selected.id;
            relationship.displayName = $scope.relationship.selected.name;
            relationship.relative = {};
            
            
            relationship.trackedEntityInstanceA = $scope.selectedRelationship.aIsToB === $scope.relationship.selected.aIsToB ? $scope.selectedTei.trackedEntityInstance : $scope.teiForRelationship.id;
            relationship.trackedEntityInstanceB = $scope.selectedRelationship.bIsToA === $scope.relationship.selected.bIsToA ? $scope.teiForRelationship.id : $scope.selectedTei.trackedEntityInstance;
            
            tei.relationships = [];
            angular.forEach($scope.selectedTei.relationships, function(rel){
                tei.relationships.push({relationship: rel.relationship, displayName: rel.displayName, trackedEntityInstanceA: rel.trackedEntityInstanceA, trackedEntityInstanceB: rel.trackedEntityInstanceB});
            });
            tei.relationships.push(relationship);
            
            TEIService.update(tei, $scope.optionSets, $scope.attributesById).then(function(response){
                if(response.response && response.response.status !== 'SUCCESS'){//update has failed
                    var dialogOptions = {
                            headerText: 'relationship_error',
                            bodyText: response.message
                        };
                    DialogService.showDialog({}, dialogOptions);
                    return;
                }
                
                relationship.relative.processed = true;
                relationship.relative.attributes = $scope.teiForRelationship;
                
                if($scope.selectedTei.relationships){
                    $scope.selectedTei.relationships.push(relationship);
                }
                else{
                    $scope.selectedTei.relationships = [relationship];
                }
                
                $modalInstance.close($scope.selectedTei.relationships);                
            });
        }        
    };
    
    //Get orgunits for the logged in user
    OrgUnitFactory.getSearchTreeRoot().then(function(response) {  
        $scope.orgUnits = response.organisationUnits;
        angular.forEach($scope.orgUnits, function(ou){
            ou.show = true;
            angular.forEach(ou.children, function(o){                    
                o.hasChildren = o.children && o.children.length > 0 ? true : false;
            });            
        });
    });
    
    //expand/collapse of search orgunit tree
    $scope.expandCollapse = function(orgUnit) {
        if( orgUnit.hasChildren ){            
            //Get children for the selected orgUnit
            OrgUnitFactory.get(orgUnit.id).then(function(ou) {                
                orgUnit.show = !orgUnit.show;
                orgUnit.hasChildren = false;
                orgUnit.children = ou.organisationUnits[0].children;                
                angular.forEach(orgUnit.children, function(ou){                    
                    ou.hasChildren = ou.children && ou.children.length > 0 ? true : false;
                });                
            });           
        }
        else{
            orgUnit.show = !orgUnit.show;   
        }        
    };
   
    //load programs for the selected orgunit (from tree)
    $scope.setSelectedSearchingOrgUnit = function(orgUnit){    
        $scope.selectedSearchingOrgUnit = orgUnit;
    };
});
