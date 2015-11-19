const {module, inject} = angular.mock;
const {spy} = sinon;

describe('Directives: d2SetFocus', () => {
    let mock$scope;
    let element;
    let $timeout;
    let render;
    beforeEach(module('d2Directives'));
    beforeEach(inject(($injector) => {
        const $compile = $injector.get('$compile');
        const $rootScope = $injector.get('$rootScope');
        $timeout = $injector.get('$timeout');
        mock$scope = $rootScope.$new();
        mock$scope.isFocused = false;
        render = (elm) =>
        {
            elm[0].focus = spy();
            $compile(elm)(mock$scope);
            mock$scope.$digest();
            $timeout.flush();
        };
    }));

    it('should set the focus when the attribute property is set to true', () => {
        var elm = angular.element('<input d2-set-focus="true" />');
        render(elm);
        expect(elm[0].focus).to.be.calledOnce;
    });

    it('should not set focus when the attribute property is set to false', () => {
        var elm = angular.element('<input d2-set-focus="false" />');
        render(elm);
        expect(elm[0].focus).to.not.be.called;
    });
});


describe('Directives: d2Enter', () => {
    let mock$scope;
    let element;
    let $timeout;
    let render;

    beforeEach(module('d2Directives'));


    beforeEach(inject(($injector) => {
        const $compile = $injector.get('$compile');
        const $rootScope = $injector.get('$rootScope');
        $timeout = $injector.get('$timeout');
        mock$scope = $rootScope.$new();
        mock$scope.search = function() {
            var a=100;
        };
        mock$scope.message="testMessage";
        render = (elm) => {
            mock$scope.search = spy();
            $compile(elm)(mock$scope);
            mock$scope.$digest();
        };
    }));

    it('should call the resgistered function on key press event', () => {
        var elm = angular.element('<input type="text" d2-enter="search(message)"/>');
        render(elm);
        var e = jQuery.Event("keydown");
        e.which = 13; // # Some key code value
        elm .trigger(e);
        expect(mock$scope.search).to.be.calledOnce;
    });
});