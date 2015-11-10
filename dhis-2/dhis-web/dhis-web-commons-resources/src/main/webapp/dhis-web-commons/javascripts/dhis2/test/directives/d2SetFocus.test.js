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

        render = () => {
            $compile(element)(mock$scope);
            mock$scope.$digest();
        };

        $timeout = $injector.get('$timeout');
        element = angular.element('<input d2-set-focus="true" />');
        element[0].focus = spy();

        mock$scope = $rootScope.$new();
        mock$scope.isFocused = false;
    }));

    it('should render correctly', () => {
        render();

        $timeout.flush();

        expect(element[0].focus).to.be.calledOnce;
    });

    it('should not set focus when the property is set to false', () => {
        element = angular.element('<input d2-set-focus="false" />');
        element[0].focus = spy();

        render();

        $timeout.flush();

        expect(element[0].focus).to.not.be.called;
    });
});
