package org.hisp.dhis.datamart.crosstab;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.hisp.dhis.DhisConvenienceTest;
import org.hisp.dhis.dataelement.Operand;
import org.hisp.dhis.datamart.crosstab.jdbc.CrossTabStore;

public class CrossTabStoreTest
    extends DhisConvenienceTest
{
    private CrossTabStore crossTabStore;
    
    private List<Operand> operands;

    // -------------------------------------------------------------------------
    // Fixture
    // -------------------------------------------------------------------------

    @Override
    public void setUpTest()
    {
        crossTabStore = (CrossTabStore) getBean( CrossTabStore.ID );
        
        operands = new ArrayList<Operand>();
        operands.add( new Operand( 1, 1 ) );
        operands.add( new Operand( 1, 2 ) );
        operands.add( new Operand( 2, 1 ) );
        operands.add( new Operand( 2, 2 ) );        
    }

    // -------------------------------------------------------------------------
    // Tests
    // -------------------------------------------------------------------------

    public void testCreateGetCrossTabTable()
    {
        crossTabStore.createCrossTabTable( operands );
        
        Map<String, Integer> columnNames = crossTabStore.getCrossTabTableColumns();
        
        assertEquals( 6, columnNames.size() );
        assertEquals( new Integer( 1 ), columnNames.get( "periodid" ) );
        assertEquals( new Integer( 2 ), columnNames.get( "sourceid" ) );
        assertEquals( new Integer( 3 ), columnNames.get( "de1_1" ) );
        assertEquals( new Integer( 4 ), columnNames.get( "de1_2" ) );
        assertEquals( new Integer( 5 ), columnNames.get( "de2_1" ) );
        assertEquals( new Integer( 6 ), columnNames.get( "de2_2" ) );        
    }
    
    public void testDropCrossTabTable()
    {
        crossTabStore.createCrossTabTable( operands );
        
        crossTabStore.dropCrossTabTable();
    }
}
