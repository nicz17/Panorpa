// ol map rendering functions

function renderMap(dLon, dLat, iZoom) {
    var oCenter = ol.proj.fromLonLat([dLon, dLat]);

    oIconStyle = new ol.style.Style({
        image: new ol.style.Icon(({
            anchor: [0.5, 32],
            anchorXUnits: 'fraction',
            anchorYUnits: 'pixels',
            src: 'location.png'
        }))
    });

    oPicStyle = new ol.style.Style({
        image: new ol.style.Icon(({
            anchor: [0.5, 32],
            anchorXUnits: 'fraction',
            anchorYUnits: 'pixels',
            src: 'location-pic.png'
        }))
    });

    oVectorSource = new ol.source.Vector({});
    var oVectorLayer = new ol.layer.Vector({
        source: oVectorSource
    });

    map = new ol.Map({
        layers: [
            new ol.layer.Tile({
		source: new ol.source.OSM({
		    //url: 'https://{a-c}.tile.openstreetmap.org/{z}/{x}/{y}.png'
		    //url: 'http://c.tiles.wmflabs.org/hillshading/${z}/${x}/${y}.png'
		    //url: 'http://toolserver.org/tiles/hikebike/{z}/{x}/{y}.png'
		    //url: 'http://tile.thunderforest.com/outdoors/{z}/{x}/{y}.png?apikey=ed03e5963fff45779c728dbfbd59a156'
		    url: 'http://tile.thunderforest.com/landscape/{z}/{x}/{y}.png?apikey=ed03e5963fff45779c728dbfbd59a156'
		})
            }),
	    oVectorLayer
        ],
        target: 'ol-map',
        view: new ol.View({
            center: oCenter,
            zoom: iZoom
        })
    });

    // Get popup div
    var element = document.getElementById('ol-popup');
    var popup = new ol.Overlay({
        element: element,
        positioning: 'bottom-center',
        stopEvent: false,
        offset: [0, -36]
    });
    map.addOverlay(popup);

    // display popup on click
    map.on('click', function(evt) {
        var feature = map.forEachFeatureAtPixel(evt.pixel,
		function(feature) {
		    return feature;
		});
        if (feature) {
            var coordinates = feature.getGeometry().getCoordinates();
	    var sHtml = feature.get('label');
	    var sUrl  = feature.get('url');
	    if (sUrl) {
		sHtml = '<a href="' + sUrl + '">' + sHtml + '</a>';
	    }
            popup.setPosition(coordinates);
	    element.innerHTML = sHtml;
	    element.style = "opacity: 0.7; background-color: white; border: 1px solid #c0c0c0; padding: 5px 10px;";
        }
    });
};

function addMapMarker(dLon, dLat, sLabel, sUrl) {
    var oPosition = ol.proj.fromLonLat([dLon, dLat]);
    var oIconFeature = new ol.Feature({
        geometry: new ol.geom.Point(oPosition),
        label: sLabel,
	url: sUrl
    });
    oIconFeature.setStyle(oIconStyle);
    oVectorSource.addFeature(oIconFeature);
};

function addPicMarker(dLon, dLat, sLabel, sUrl) {
    var oPosition = ol.proj.fromLonLat([dLon, dLat]);
    var oIconFeature = new ol.Feature({
        geometry: new ol.geom.Point(oPosition),
        label: sLabel,
	url: sUrl
    });
    oIconFeature.setStyle(oPicStyle);
    oVectorSource.addFeature(oIconFeature);
};

function addMapTrack(sGpxFile) {
    var lTrack = new ol.layer.Vector({
        source: new ol.source.Vector({
            url: sGpxFile,
            format: new ol.format.GPX(),
        }),
        style: new ol.style.Style({
            stroke: new ol.style.Stroke({
                color: 'red',
                width: 2
            })
        })
    });
    map.addLayer(lTrack);
};
