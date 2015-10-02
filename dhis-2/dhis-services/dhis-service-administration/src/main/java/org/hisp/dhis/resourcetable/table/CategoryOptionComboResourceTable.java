package org.hisp.dhis.resourcetable.table;

import java.util.List;
import java.util.Optional;

import org.hisp.dhis.dataelement.DataElementCategoryOptionCombo;
import org.hisp.dhis.resourcetable.ResourceTable;

public class CategoryOptionComboResourceTable
    extends ResourceTable<DataElementCategoryOptionCombo>
{

    @Override
    public String getTableName()
    {
        return "_dataelementcategoryoptioncombo";
    }

    @Override
    public String getCreateTempTableStatement()
    {
        String sql = "CREATE TABLE " + getTempTableName() + " (" +
            "dataelementid INTEGER NOT NULL, " +
            "dataelementuid VARCHAR(11) NOT NULL, " +
            "categoryoptioncomboid INTEGER NOT NULL, " +
            "categoryoptioncombouid VARCHAR(11) NOT NULL)";
        
        return sql;
    }

    @Override
    public Optional<String> getPopulateTempTableStatement()
    {
        String sql = 
            "insert into " + getTempTableName() + 
            " (dataelementid, dataelementuid, categoryoptioncomboid, categoryoptioncombouid) " +
            "select de.dataelementid as dataelementid, de.uid as dataelementuid, " +
            "coc.categoryoptioncomboid as categoryoptioncomboid, coc.uid as categoryoptioncombouid " +
            "from dataelement de " +
            "join categorycombos_optioncombos cc on de.categorycomboid = cc.categorycomboid " +
            "join categoryoptioncombo coc on cc.categoryoptioncomboid = coc.categoryoptioncomboid";
        
        return Optional.of( sql );        
    }

    @Override
    public Optional<List<Object[]>> getPopulateTempTableContent()
    {
        return Optional.empty();
    }

    @Override
    public Optional<String> getCreateIndexStatement()
    {
        String name = "in_dataelementcategoryoptioncombo_" + getRandomSuffix();
        
        String sql = "create index " + name + " on " + getTempTableName() + "(dataelementuid, categoryoptioncombouid)";
        
        return Optional.of( sql );
    }
}
