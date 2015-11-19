const {module, inject} = angular.mock;
const {spy} = sinon;

describe('Directives: d2SetFocus', () => {
    let mock$scope;
    let element;
    let $timeout;
    let render;
    beforeEach(module('d2Directives'));
    beforeEach(inject(($injector) = > {
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

    it('should set the focus when the attribute property is set to true', () = > {
        var elm = angular.element('<input d2-set-focus="true" />');
        render(elm);
        expect(elm[0].focus).to.be.calledOnce;
    });

    it('should not set focus when the attribute property is set to false', () = > {
        var elm = angular.element('<input d2-set-focus="false" />');
        render(elm);
        expect(elm[0].focus).to.not.be.called;
    });
});