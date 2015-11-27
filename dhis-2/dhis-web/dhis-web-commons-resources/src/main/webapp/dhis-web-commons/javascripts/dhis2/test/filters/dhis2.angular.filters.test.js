const {module, inject} = angular.mock;
const {spy} = sinon;
describe('Filters: d2Filters', () => {

    beforeEach(module('d2Filters'));

    describe('d2Filter: trimquotesFilter', () => {
        let trimquotesFilter;
        beforeEach(inject(($injector) => {
            trimquotesFilter = $injector.get('trimquotesFilter');
        }));

        it('should trim the initial single and double quotes', function() {
            var trimmedString = trimquotesFilter('"testString"');
            expect(trimmedString).to.equal('testString');
       });

    });
});