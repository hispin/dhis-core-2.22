package org.hisp.dhis.mapping;

/*
 * Copyright (c) 2004-2007, University of Oslo
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * * Redistributions of source code must retain the above copyright notice, this
 *   list of conditions and the following disclaimer.
 * * Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the documentation
 *   and/or other materials provided with the distribution.
 * * Neither the name of the HISP project nor the names of its contributors may
 *   be used to endorse or promote products derived from this software without
 *   specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;

import org.hisp.dhis.indicator.Indicator;
import org.hisp.dhis.indicator.IndicatorService;
import org.hisp.dhis.organisationunit.OrganisationUnit;
import org.hisp.dhis.organisationunit.OrganisationUnitLevel;
import org.hisp.dhis.organisationunit.OrganisationUnitService;

/**
 * @author Jan Henrik Overland
 * @version $Id$
 */
public class DefaultMappingService
    implements MappingService
{
    // -------------------------------------------------------------------------
    // Dependencies
    // -------------------------------------------------------------------------

    private MappingStore mappingStore;

    public void setMappingStore( MappingStore mappingStore )
    {
        this.mappingStore = mappingStore;
    }

    private OrganisationUnitService organisationUnitService;

    public void setOrganisationUnitService( OrganisationUnitService organisationUnitService )
    {
        this.organisationUnitService = organisationUnitService;
    }
    
    private IndicatorService indicatorService;
    
    public void setIndicatorService( IndicatorService indicatorService )
    {
        this.indicatorService = indicatorService;
    }

    // -------------------------------------------------------------------------
    // MappingService implementation
    // -------------------------------------------------------------------------

    // -------------------------------------------------------------------------
    // Map
    // -------------------------------------------------------------------------

    public int addMap( Map map )
    {
        return mappingStore.addMap( map );
    }

    public int addMap( String name, String mapLayerPath, String type, int organisationUnitId, int organisationUnitLevelId, String uniqueColumn,
        String nameColumn, String longitude, String latitude, int zoom )
    {
        OrganisationUnit organisationUnit = organisationUnitService.getOrganisationUnit( organisationUnitId );

        OrganisationUnitLevel organisationUnitLevel = organisationUnitService
            .getOrganisationUnitLevel( organisationUnitLevelId );

        Set<String> staticMapLayerPaths = null;

        Map map = new Map( name, mapLayerPath, type, organisationUnit, organisationUnitLevel, uniqueColumn, nameColumn, longitude,
            latitude, zoom, staticMapLayerPaths );

        return addMap( map );
    }

    public void addOrUpdateMap( String name, String mapLayerPath, String type, int organisationUnitId, int organisationUnitLevelId,
        String uniqueColumn, String nameColumn, String longitude, String latitude, int zoom )
    {
        Map map = getMapByMapLayerPath( mapLayerPath );

        if ( map != null )
        {
            map.setName( name );
            map.setUniqueColumn( uniqueColumn );
            map.setNameColumn( nameColumn );
            map.setLongitude( longitude );
            map.setLatitude( latitude );
            map.setZoom( zoom );

            updateMap( map );
        }
        else
        {
            OrganisationUnit organisationUnit = organisationUnitService.getOrganisationUnit( organisationUnitId );

            OrganisationUnitLevel organisationUnitLevel = organisationUnitService
                .getOrganisationUnitLevel( organisationUnitLevelId );

            map = new Map( name, mapLayerPath, type, organisationUnit, organisationUnitLevel, uniqueColumn, nameColumn, longitude,
                latitude, zoom, null );

            addMap( map );
        }
    }

    public void updateMap( Map map )
    {
        mappingStore.updateMap( map );
    }

    public void deleteMap( Map map )
    {
        mappingStore.deleteMap( map );
    }

    public void deleteMapByMapLayerPath( String mapLayerPath )
    {
        Map map = getMapByMapLayerPath( mapLayerPath );

        deleteMap( map );
    }

    public Map getMap( int id )
    {
        return mappingStore.getMap( id );
    }

    public Map getMapByMapLayerPath( String mapLayerPath )
    {
        return mappingStore.getMapByMapLayerPath( mapLayerPath );
    }

    public Collection<Map> getAllMaps()
    {
        return mappingStore.getAllMaps();
    }

    public Collection<Map> getMapsAtLevel( OrganisationUnitLevel organisationUnitLevel )
    {
        return mappingStore.getMapsAtLevel( organisationUnitLevel );
    }

    // -------------------------------------------------------------------------
    // MapOrganisationUnitRelation
    // -------------------------------------------------------------------------

    public int addMapOrganisationUnitRelation( MapOrganisationUnitRelation mapOrganisationUnitRelation )
    {
        return mappingStore.addMapOrganisationUnitRelation( mapOrganisationUnitRelation );
    }

    public int addMapOrganisationUnitRelation( String mapLayerPath, int organisationUnitId, String featureId )
    {
        Map map = getMapByMapLayerPath( mapLayerPath );

        OrganisationUnit organisationUnit = organisationUnitService.getOrganisationUnit( organisationUnitId );

        MapOrganisationUnitRelation mapOrganisationUnitRelation = new MapOrganisationUnitRelation( map,
            organisationUnit, featureId );

        return addMapOrganisationUnitRelation( mapOrganisationUnitRelation );
    }

    public void addOrUpdateMapOrganisationUnitRelation( String mapLayerPath, int organisationUnitId, String featureId )
    {
        Map map = getMapByMapLayerPath( mapLayerPath );

        OrganisationUnit organisationUnit = organisationUnitService.getOrganisationUnit( organisationUnitId );

        MapOrganisationUnitRelation mapOrganisationUnitRelation = getMapOrganisationUnitRelation( map,
            organisationUnit );

        if ( mapOrganisationUnitRelation != null )
        {
            mapOrganisationUnitRelation.setFeatureId( featureId );

            updateMapOrganisationUnitRelation( mapOrganisationUnitRelation );
        }
        else
        {
            mapOrganisationUnitRelation = new MapOrganisationUnitRelation( map, organisationUnit, featureId );

            addMapOrganisationUnitRelation( mapOrganisationUnitRelation );
        }
    }

    public void updateMapOrganisationUnitRelation( MapOrganisationUnitRelation mapOrganisationUnitRelation )
    {
        mappingStore.updateMapOrganisationUnitRelation( mapOrganisationUnitRelation );
    }

    public void deleteMapOrganisationUnitRelation( MapOrganisationUnitRelation mapOrganisationUnitRelation )
    {
        mappingStore.deleteMapOrganisationUnitRelation( mapOrganisationUnitRelation );
    }

    public MapOrganisationUnitRelation getMapOrganisationUnitRelation( int id )
    {
        return mappingStore.getMapOrganisationUnitRelation( id );
    }

    public MapOrganisationUnitRelation getMapOrganisationUnitRelation( Map map, OrganisationUnit organisationUnit )
    {
        return mappingStore.getMapOrganisationUnitRelation( map, organisationUnit );
    }

    public Collection<MapOrganisationUnitRelation> getAllMapOrganisationUnitRelations()
    {
        return mappingStore.getAllMapOrganisationUnitRelations();
    }

    public Collection<MapOrganisationUnitRelation> getMapOrganisationUnitRelationByMap( Map map )
    {
        return mappingStore.getMapOrganisationUnitRelationByMap( map );
    }

    public Collection<MapOrganisationUnitRelation> getAvailableMapOrganisationUnitRelations( Map map )
    {
        Collection<OrganisationUnit> organisationUnits = organisationUnitService.getOrganisationUnitsAtLevel( map
            .getOrganisationUnitLevel().getLevel() );

        Collection<MapOrganisationUnitRelation> relations = new ArrayList<MapOrganisationUnitRelation>();

        for ( OrganisationUnit unit : organisationUnits )
        {
            MapOrganisationUnitRelation relation = getMapOrganisationUnitRelation( map, unit );

            relations.add( relation != null ? relation : new MapOrganisationUnitRelation( map, unit, null ) );
        }

        return relations;
    }

    public Collection<MapOrganisationUnitRelation> getAvailableMapOrganisationUnitRelations( String mapLayerPath )
    {
        Map map = getMapByMapLayerPath( mapLayerPath );

        return getAvailableMapOrganisationUnitRelations( map );
    }

    public int deleteMapOrganisationUnitRelations( OrganisationUnit organisationUnit )
    {
        return mappingStore.deleteMapOrganisationUnitRelations( organisationUnit );
    }

    public int deleteMapOrganisationUnitRelations( Map map )
    {
        return mappingStore.deleteMapOrganisationUnitRelations( map );
    }
    
    // -------------------------------------------------------------------------
    // LegendSet
    // -------------------------------------------------------------------------    
    
    public int addMapLegendSet( MapLegendSet legendSet )
    {
        return mappingStore.addMapLegendSet( legendSet );
    }
    
    public void updateMapLegendSet( MapLegendSet legendSet )
    {
        mappingStore.updateMapLegendSet( legendSet );
    }
    
    public void deleteMapLegendSet( MapLegendSet legendSet )
    {
        mappingStore.deleteMapLegendSet( legendSet );
    }
    
    public MapLegendSet getMapLegendSet( int id )
    {
        return mappingStore.getMapLegendSet( id );
    }
    
    public MapLegendSet getMapLegendSetByIndicator( int indicatorId )
    {
        Indicator indicator = indicatorService.getIndicator( indicatorId );
        
        Collection<MapLegendSet> legendSets = mappingStore.getAllMapLegendSets();
        
        for ( MapLegendSet legendSet : legendSets )
        {
            if ( legendSet.getIndicators().contains( indicator ))
            {
                return legendSet;
            }
        }
        
        return null;
    }
    
    public Collection<MapLegendSet> getAllMapLegendSets()
    {
        return mappingStore.getAllMapLegendSets();
    }
    
    public boolean indicatorHasMapLegendSet( int indicatorId )
    {
        Indicator indicator = indicatorService.getIndicator( indicatorId );
        
        Collection<MapLegendSet> legendSets = mappingStore.getAllMapLegendSets();
        
        for ( MapLegendSet legendSet : legendSets )
        {
            if ( legendSet.getIndicators().contains( indicator ))
            {
                return true;
            }
        }
        
        return false;
    }
}