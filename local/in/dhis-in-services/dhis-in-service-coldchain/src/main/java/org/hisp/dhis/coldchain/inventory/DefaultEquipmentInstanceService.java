package org.hisp.dhis.coldchain.inventory;

import java.util.Collection;
import java.util.List;

import org.hisp.dhis.organisationunit.OrganisationUnit;
import org.springframework.transaction.annotation.Transactional;

@Transactional
public class DefaultEquipmentInstanceService implements EquipmentInstanceService
{

    // -------------------------------------------------------------------------
    // Dependencies
    // -------------------------------------------------------------------------

    private EquipmentInstanceStore equipmentInstanceStore;

    public void setEquipmentInstanceStore( EquipmentInstanceStore equipmentInstanceStore )
    {
        this.equipmentInstanceStore = equipmentInstanceStore;
    }

    private EquipmentDetailsService equipmentDetailsService;
    
    public void setEquipmentDetailsService( EquipmentDetailsService equipmentDetailsService )
    {
        this.equipmentDetailsService = equipmentDetailsService;
    }
    
    // -------------------------------------------------------------------------
    // EquipmentInstance
    // -------------------------------------------------------------------------
    
    @Override
    public int addEquipmentInstance( EquipmentInstance equipmentInstance )
    {
        return equipmentInstanceStore.addEquipmentInstance( equipmentInstance );
    }
    @Override
    public void deleteEquipmentInstance( EquipmentInstance equipmentInstance )
    {
        equipmentInstanceStore.deleteEquipmentInstance( equipmentInstance );
    }
    @Override
    public Collection<EquipmentInstance> getAllEquipmentInstance()
    {
        return equipmentInstanceStore.getAllEquipmentInstance();
    }
    @Override
    public void updateEquipmentInstance( EquipmentInstance equipmentInstance )
    {
        equipmentInstanceStore.updateEquipmentInstance( equipmentInstance );
    }
    
    public int createEquipment( EquipmentInstance equipmentInstance, List<EquipmentDetails> equipmentDetails )
    {
        int equipmentInstanceId = addEquipmentInstance( equipmentInstance );
        
        for( EquipmentDetails equipment : equipmentDetails )
        {
            equipmentDetailsService.addEquipmentDetails( equipment );
        }
        
        return equipmentInstanceId;
    }

    
    public Collection<EquipmentInstance> getEquipmentInstances( OrganisationUnit orgUnit )
    {
        return equipmentInstanceStore.getEquipmentInstances( orgUnit );
    }

    public Collection<EquipmentInstance> getEquipmentInstances( OrganisationUnit orgUnit, InventoryType inventoryType )
    {
        return equipmentInstanceStore.getEquipmentInstances( orgUnit, inventoryType );
    }

    public Collection<EquipmentInstance> getEquipmentInstances( OrganisationUnit orgUnit, InventoryType inventoryType, int min, int max )
    {
        return equipmentInstanceStore.getEquipmentInstances( orgUnit, inventoryType, min, max );
    }

    @Override
    public int getCountEquipmentInstance( OrganisationUnit orgUnit, InventoryType inventoryType )
    {
        return equipmentInstanceStore.getCountEquipmentInstance( orgUnit, inventoryType );
    }

}
