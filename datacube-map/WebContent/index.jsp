<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>Insert title here</title>
<link rel="stylesheet" type="text/css" href="js/leaflet/leaflet.css">
<link rel="stylesheet" href="js/jrange/jquery.range.css">
<script type="text/javascript" src="js/d3.min.js"></script>
<script type="text/javascript" src="js/leaflet/leaflet.js"></script>
<script type="text/javascript" src="js/jquery-3.4.1.min.js"></script>
<script src="js/jrange/jquery.range.js"></script>
<style type="text/css">
#mapDiv {
	height: 800px;
	width: 1200px;
}
</style>
</head>
<body>
	<div id="mapDiv"></div>
	<script>
		var url = 'https://api.tiles.mapbox.com/v4/{id}/{z}/{x}/{y}.png?access_token=pk.eyJ1Ijoia2FuZXdhbmciLCJhIjoiY2pwM2UxNHNkMGF1MzNwc2FtMnNhdXJsMCJ9.KZpCBtizDeltZO6JhGc6_w';
		var tweets = L.layerGroup();
		var areaRct = L.layerGroup();
		var p1, p2;
		var overlayMaps = {
			"Tweets" : tweets,
			"AreaInfo" : areaRct
		};
		var citiesControl
		//map�
		var blackMap = L
				.tileLayer(
						'https://api.mapbox.com/styles/v1/{id}/tiles/256/{z}/{x}/{y}?access_token={accessToken}',
						{
							attribution : 'Map data &copy; <a href="https://www.openstreetmap.org/">OpenStreetMap</a> contributors, <a href="https://creativecommons.org/licenses/by-sa/2.0/">CC-BY-SA</a>, Imagery © <a href="https://www.mapbox.com/">Mapbox</a>',
							maxZoom : 18,
							id : 'mapbox/dark-v9',
							accessToken : 'pk.eyJ1IjoiY29va2llcyIsImEiOiJjaW9sOGpwYjgwMGJtdmtqYmFieGYwcGR5In0.ot-rN7HEza9xJSijmrAOUQ'
						});
		var streatMap = L
				.tileLayer(
						'https://api.mapbox.com/styles/v1/{id}/tiles/256/{z}/{x}/{y}?access_token={accessToken}',
						{
							attribution : 'Map data &copy; <a href="https://www.openstreetmap.org/">OpenStreetMap</a> contributors, <a href="https://creativecommons.org/licenses/by-sa/2.0/">CC-BY-SA</a>, Imagery © <a href="https://www.mapbox.com/">Mapbox</a>',
							maxZoom : 18,
							id : 'mapbox/streets-v11',
							accessToken : 'pk.eyJ1IjoiY29va2llcyIsImEiOiJjaW9sOGpwYjgwMGJtdmtqYmFieGYwcGR5In0.ot-rN7HEza9xJSijmrAOUQ'
						});
		var baseMaps = {
			"BlackMap" : blackMap,
			"StreatMap" : streatMap
		}
		var leafletMap = L.map('mapDiv', {
			center : [ 39.774769, -99.755859 ],
			zoom : 5,
			layers : [ blackMap, tweets, areaRct ]
		});

		var layerControl = L.control.layers(baseMaps, overlayMaps).addTo(
				leafletMap);

		var zoom = 20 - leafletMap._zoom
		var config = {
			color : 'blue',
			fillColor : '#1af419',
			weight : 0,
			fillOpacity : 1
		};

		var popup = L.popup();
		function onMapClick(e) {
			if (leafletMap.hasLayer(areaRct)){
				if (p1 == null)
					p1 = e.latlng;
				else if (p2 == null){
					p2 = e.latlng;
					var ajaxTime = new Date().getTime();
					var _type;
					if ($("#Android").is(":checked") && $("#IPhone").is(":checked")) {
						config.fillColor = '#f03';
						_type = 0;
					} else if (!$("#Android").is(":checked")
							&& $("#IPhone").is(":checked")) {
						config.fillColor = '#1af419';
						_type = 1;
					} else if ($("#Android").is(":checked")
							&& !$("#IPhone").is(":checked")) {
						config.fillColor = '#9aabfa';
						_type = 2;
					} else
						return;
					jQuery.ajax({
						url : "queryTweets",
						type : "get",
						data : {
							"ltLat" : Math.max(p1.lat, p2.lat),
							"ltLon" : Math.min(p1.lng, p2.lng),
							"rbLat" : Math.min(p1.lat, p2.lat),
							"rbLon" : Math.max(p1.lng, p2.lng),
							"zoom" : leafletMap._zoom + 2,
							"type" : _type
						},
						dataType : "text",
						success : function(msg) {
							console.info('\t' + (new Date().getTime() - ajaxTime));
							var nodes = jQuery.parseJSON(msg);
							var count = 0;
							for (var i = 0; i < nodes.length; i++)
								count += nodes[i].z;
							areaRct.addLayer(L.rectangle([ p1, p2 ], {color: "#ff7800", weight: 1}).bindPopup(
							"Count: " + count));
						}
					});				
				}
				else {
					p1 = null;
					p2 = null;
					areaRct.clearLayers();
					return;
				}
			}			
			popup.setLatLng(e.latlng).setContent(
					"You clicked the map at " + e.latlng.toString()).openOn(
					leafletMap);
		}
		leafletMap.on('click', onMapClick);

		function queryGet() {
			var ajaxTime = new Date().getTime();
			var _type;
			if ($("#Android").is(":checked") && $("#IPhone").is(":checked")) {
				config.fillColor = '#f03';
				_type = 0;
			} else if (!$("#Android").is(":checked")
					&& $("#IPhone").is(":checked")) {
				config.fillColor = '#1af419';
				_type = 1;
			} else if ($("#Android").is(":checked")
					&& !$("#IPhone").is(":checked")) {
				config.fillColor = '#9aabfa';
				_type = 2;
			} else
				return;

			jQuery.ajax({
				url : "queryTweets",
				type : "get",
				data : {
					"ltLat" : leafletMap.getBounds().getNorthWest().lat,
					"ltLon" : leafletMap.getBounds().getNorthWest().lng,
					"rbLat" : leafletMap.getBounds().getSouthEast().lat,
					"rbLon" : leafletMap.getBounds().getSouthEast().lng,
					"zoom" : leafletMap._zoom + parseInt($('#agg-slider').jRange('getValue')),
					"type" : _type
				},
				dataType : "text",
				success : function(msg) {
					console.info((new Date().getTime() - ajaxTime));
					var nodes = jQuery.parseJSON(msg);
					tweets.clearLayers();
					wO = wOffset(leafletMap._zoom + parseInt($('#agg-slider').jRange('getValue')));
					hO = hOffset(leafletMap._zoom + parseInt($('#agg-slider').jRange('getValue')));
					for (var i = 0; i < nodes.length; i++) {
						var node = nodes[i];
						/*
						tweets.addLayer(L.circle([ node.y, node.x ],
								(30 + node.z) * zoom - 100, config).bindPopup(
								"Count:" + node.z));
						*/
						updateConfig(node.z)
						tweets.addLayer(L.rectangle([[ node.y - hO, node.x - wO ], [ node.y + hO, node.x + wO ]], 
								config).bindPopup(
								"Count:" + node.z));
					}
				}
			});
		}
		
		function updateConfig(count){
			config.fillOpacity = 0
			for (var i = 0; i < 10; i++){
				config.fillOpacity += 0.1
				if(count <= Math.pow(3, i))
					return
			}
		}
		
		function wOffset(zoom){
			w = 3600000000
			for (var i = 0; i <= zoom; i++)
				w = w / 2
			return w / 10000000.0
		}
		
		function hOffset(zoom){
			h = 1800000000
			for (var i = 0; i <= zoom; i++)
				h = h / 2
			return h / 10000000.0
		}
		
		function updateAggsplit() {
			jQuery.ajax({
				url : "aggSplit",
				type : "get",
				data : {
					"type" : 'update',
					"split" : $("#AggSplit").val()
				},
				dataType : "text",
				success : function(msg) {
					$("#AggSplit").attr("value",msg)
					$('#AggSplit').val(msg);
				}
			});	
		}
		
		function reciveAggsplit() {
			jQuery.ajax({
				url : "aggSplit",
				type : "get",
				data : {
					"type" : 'receive'
				},
				dataType : "text",
				success : function(msg) {
					$("#AggSplit").attr("value",msg)
					$('#AggSplit').val(msg);
				}
			});	
		}

		//leafletMap.on('zoomend', queryGet);
		leafletMap.on('moveend', queryGet);
		$(document).ready(function(){
            $('#agg-slider').jRange({
                from: 2,
                to: 7,
                step: 1, 
                format: '%s',
                scale: [2, 3, 4, 5, 6, 7],
                width: 300,
                showLabels: true,
                snap: true
            });
        });
	</script>
	<br>
	<input type="hidden" id="agg-slider" value="5" />
	<br>
	<button type="button" onClick="queryGet()">QueryTest</button>
	<input type="checkbox" id="Android" name="check">Android
	</input>
	<input type="checkbox" id="IPhone" name="check">IPhone
	</input>
	<br>
	<button type="button" onClick="updateAggsplit()">Update</button>
	<input type="text" id="AggSplit" value="asdfasdf"></input>
	<button type="button" onClick="reciveAggsplit()">Receive</button>
</body>
</html>