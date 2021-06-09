var p1, p2;
var last_nodes;
var last_ltLat;
var last_ltLon;
var last_rbLat;
var last_rbLon;
var last_zoom;
var last_zoom2=0;
var delay = 50
var last_type;
var his_queries = []
var his_map = []
var ind = -1

function getDRegion(last_lt_lat,last_lt_lon,last_rb_lat,last_rb_lon,lt_lat,lt_lon,rb_lat,rb_lon){
	if(last_lt_lon<lt_lon && lt_lon<last_rb_lon && last_rb_lat<rb_lat && rb_lat<last_lt_lat){ // lb
		var r1 = [lt_lat,lt_lon,last_lt_lat,rb_lon];
		var r2 = [last_lt_lat,last_rb_lon,rb_lat,rb_lon];
		return [r1,r2];
	}
	if(last_lt_lon<lt_lon && lt_lon<last_rb_lon && last_rb_lat<lt_lat && lt_lat<last_lt_lat){ // lt
		var r1 = [lt_lat,last_rb_lon,rb_lat,rb_lon];
		var r2 = [last_rb_lat,lt_lon,rb_lat,last_rb_lon];
		return [r1,r2];
	}
	if(last_lt_lon<rb_lon && rb_lon<last_rb_lon && last_rb_lat<rb_lat && rb_lat<last_lt_lat){ // rb
		var r1 = [lt_lat,lt_lon,last_lt_lat,rb_lon];
		var r2 = [last_lt_lat,lt_lon,rb_lat,last_lt_lon];
		return [r1,r2];
	}
	if(last_lt_lon<rb_lon && rb_lon<last_rb_lon && last_rb_lat<lt_lat && lt_lat<last_lt_lat){ // rt
		var r1 = [lt_lat,lt_lon,rb_lat,last_lt_lon];
		var r2 = [last_rb_lat,last_lt_lon,rb_lat,rb_lon];
		return [r1,r2];
	}
}

function jsReadFiles(files) {
	if (files.length) {
		var file = files[0];
		var reader = new FileReader();//new一个FileReader实例
		reader.onload = function() {
				his_queries = jQuery.parseJSON(this.result);
				ind = -1
			}
		reader.readAsText(file)
	}
}

function queryGet2() {
	if(last_nodes == null){
		queryGet2();
		return
	}
	if(last_zoom != leafletMap._zoom + parseInt($('#agg-slider').jRange('getValue'))){
		queryGet2();
		return
	}
	var ajaxTime = new Date().getTime();
	var _type;
	if ($("#Android").is(":checked") && $("#IPhone").is(":checked"))
		_type = 0;
	else if (!$("#Android").is(":checked") && $("#IPhone").is(":checked"))
		_type = 1;
	else if ($("#Android").is(":checked") && !$("#IPhone").is(":checked"))
		_type = 2;
	else
		return;
	if(last_type != _type){
		queryGet2();
		return
	}
	var this_ltLat = leafletMap.getBounds().getNorthWest().lat;
	var this_ltLon = leafletMap.getBounds().getNorthWest().lng;
	var this_rbLat = leafletMap.getBounds().getSouthEast().lat;
	var this_rbLon = leafletMap.getBounds().getSouthEast().lng;
	var timeChart = echarts.init(document.getElementById('time_zoom'));
	var opt = timeChart.getOption();
	rdata = {
			"ltLat" : leafletMap.getBounds().getNorthWest().lat,
			"ltLon" : leafletMap.getBounds().getNorthWest().lng,
			"rbLat" : leafletMap.getBounds().getSouthEast().lat,
			"rbLon" : leafletMap.getBounds().getSouthEast().lng,
			"zoom" : last_zoom,
			"decive" : last_type,
			"type": 0,
			"from": categoryData[opt.dataZoom[0].startValue],
			"to": categoryData[opt.dataZoom[0].endValue]
		}
	his_queries.push(rdata);
	var mapd = {
			"center" : leafletMap.getCenter(),
			"zoom" : leafletMap.getZoom()
	}
	his_map.push(mapd)
	var DR = getDRegion(last_ltLat,last_ltLon,last_rbLat,last_rbLon,this_ltLat,this_ltLon,this_rbLat,this_rbLon);
	last_ltLat = this_ltLat;
	last_ltLon = this_ltLon;
	last_rbLat = this_rbLat;
	last_rbLon = this_rbLon;
	r1 = {
			"ltLat" : DR[0][0],
			"ltLon" : DR[0][1],
			"rbLat" : DR[0][2],
			"rbLon" : DR[0][3],
			"zoom" : last_zoom,
			"decive" : last_type,
			"type": 0,
			"from": rdata.from,
			"to": rdata.to
		}
	jQuery.ajax({
		url : "queryTweets",
		type : "get",
		data : r1,
		dataType : "text",
		success : function(msg) {
			console.info((new Date().getTime() - ajaxTime));
			var nodes = jQuery.parseJSON(msg);
			// last_nodes = nodes;
			drawMap(nodes);
		}
	});
	r2 = {
			"ltLat" : DR[1][0],
			"ltLon" : DR[1][1],
			"rbLat" : DR[1][2],
			"rbLon" : DR[1][3],
			"zoom" : last_zoom,
			"decive" : last_type,
			"type": 0,
			"from": rdata.from,
			"to": rdata.to
		}
	jQuery.ajax({
		url : "queryTweets",
		type : "get",
		data : r2,
		dataType : "text",
		success : function(msg) {
			console.info((new Date().getTime() - ajaxTime));
			var nodes = jQuery.parseJSON(msg);
			// last_nodes = nodes;
			drawMap(nodes);
		}
	});
}

function onMapClick(e) {
	if (leafletMap.hasLayer(areaRct)) {
		if (p1 == null)
			p1 = e.latlng;
		else if (p2 == null) {
			p2 = e.latlng;
			var ajaxTime = new Date().getTime();
			var _type;
			if ($("#Android").is(":checked") && $("#IPhone").is(":checked"))
				_type = 0;
			else if (!$("#Android").is(":checked") && $("#IPhone").is(":checked"))
				_type = 1;
			else if ($("#Android").is(":checked") && !$("#IPhone").is(":checked"))
				_type = 2;
			else
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
					"decive" : _type,
					"type": 0,
					"from": 20130101,
					"to": 20160101
				},
				dataType : "text",
				success : function(msg) {
					console.info('\t' + (new Date().getTime() - ajaxTime));
					var nodes = jQuery.parseJSON(msg);
					drawAera(nodes);
				}
			});
		} else {
			p1 = null;
			p2 = null;
			areaRct.clearLayers();
			return;
		}
	}
	popup.setLatLng(e.latlng).setContent(
			"You clicked the map at " + e.latlng.toString()).openOn(leafletMap);
}

function sleep(time) {
    var startTime = new Date().getTime() + parseInt(time, 10);
    while(new Date().getTime() < startTime) {}
};

function setDelay(dd){
	delay = dd;
}

function reTest(dd) {
	ind++;
	if (ind < his_queries.length){
		if (dd==null) dd = 4000;
		leafletMap.setView(his_map[ind].center,his_map[ind].zoom)
		setTimeout(function(){reTest(dd);}, dd);
//		his_queries[ind].decive = his_queries[ind].type;
//		his_queries[ind].type = 0;
//		var ajaxTime = new Date().getTime();
//		jQuery.ajax({
//			url : "queryTweets",
//			type : "get",
//			data : his_queries[ind],
//			dataType : "text",
//			success : function(msg) {
//				console.info((new Date().getTime() - ajaxTime));
////				leafletMap.setView([(his_queries[ind].ltLat + his_queries[ind].rbLat) / 2, 
////					(his_queries[ind].ltLon + his_queries[ind].rbLon) / 2], 
////					his_queries[ind].zoom - 5)
//				leafletMap.setView(his_map[ind].center,his_map[ind].zoom)
//				var nodes = jQuery.parseJSON(msg);
//				last_nodes = nodes;
//				tweets.clearLayers();
//				drawMap(nodes);
//				setTimeout(function(){reTest(delay);}, 5000);
//			}
//		}); 
	}
	else{
		ind = -1;
		console.info("finish test");
	}
}

function queryGet(){
	var ajaxTime = new Date().getTime();
	var _type;
	if ($("#Android").is(":checked") && $("#IPhone").is(":checked"))
		_type = 0;
	else if (!$("#Android").is(":checked") && $("#IPhone").is(":checked"))
		_type = 1;
	else if ($("#Android").is(":checked") && !$("#IPhone").is(":checked"))
		_type = 2;
	else
		return;
	last_ltLat = leafletMap.getBounds().getNorthWest().lat;
	last_ltLon = leafletMap.getBounds().getNorthWest().lng;
	last_rbLat = leafletMap.getBounds().getSouthEast().lat;
	last_rbLon = leafletMap.getBounds().getSouthEast().lng;
	last_zoom = leafletMap._zoom + parseInt($('#agg-slider').jRange('getValue'));
	last_type = _type;
	var timeChart = echarts.init(document.getElementById('time_zoom'));
	var opt = timeChart.getOption();
	rdata = {
			"ltLat" : last_ltLat,
			"ltLon" : last_ltLon,
			"rbLat" : last_rbLat,
			"rbLon" : last_rbLon,
			"zoom" : last_zoom,
			"decive" : last_type,
			"type": 0,
			"from": categoryData[opt.dataZoom[0].startValue],
			"to": categoryData[opt.dataZoom[0].endValue]
		}
	if(ind == -1){
		his_queries.push(rdata);
		var mapd = {
				"center" : leafletMap.getCenter(),
				"zoom" : leafletMap.getZoom()
		}
		his_map.push(mapd)
	}
	jQuery.ajax({
		url : "queryTweets",
		type : "get",
		data : rdata,
		dataType : "text",
		success : function(msg) {
			var _delay = 0
			if (ind != -1 && last_zoom2 != leafletMap.getZoom()){
				_delay = _delay + delay*Math.abs(leafletMap.getZoom()-last_zoom2)*(Math.random()+0.5);
			}
			setTimeout(function(){
				console.info((new Date().getTime() - ajaxTime));
				var nodes = jQuery.parseJSON(msg);
				last_nodes = nodes;
				tweets.clearLayers();
				drawMap(nodes);
			}, _delay)
			last_zoom2 = leafletMap.getZoom();
		}
	});
}

function queryTime() {
	var ajaxTime = new Date().getTime();
	var _type;
	if ($("#Android").is(":checked") && $("#IPhone").is(":checked"))
		_type = 0;
	else if (!$("#Android").is(":checked") && $("#IPhone").is(":checked"))
		_type = 1;
	else if ($("#Android").is(":checked") && !$("#IPhone").is(":checked"))
		_type = 2;
	else
		return;
	
	jQuery.ajax({
		url : "queryTweets",
		type : "get",
		data : {
			"ltLat" : leafletMap.getBounds().getNorthWest().lat,
			"ltLon" : leafletMap.getBounds().getNorthWest().lng,
			"rbLat" : leafletMap.getBounds().getSouthEast().lat,
			"rbLon" : leafletMap.getBounds().getSouthEast().lng,
			"zoom" : leafletMap._zoom
					+ parseInt($('#agg-slider').jRange('getValue')),
			"decive" : _type,
			"type": 1,
			"timeLevel": 3
		},
		dataType : "text",
		success : function(msg) {
			console.info((new Date().getTime() - ajaxTime));
			var nodes = jQuery.parseJSON(msg);
			drawTimeZoom(nodes);
		}
	});
}


function queryCompare(){
	jQuery.ajax({
		url : "queryTweets",
		type : "get",
		data : {
			"ltLat" : leafletMap.getBounds().getNorthWest().lat,
			"ltLon" : leafletMap.getBounds().getNorthWest().lng,
			"rbLat" : leafletMap.getBounds().getSouthEast().lat,
			"rbLon" : leafletMap.getBounds().getSouthEast().lng,
			"zoom" : leafletMap._zoom
					+ parseInt($('#agg-slider').jRange('getValue')),
			"decive" : 1,
			"type": 1,
			"timeLevel": 3
		},
		dataType : "text",
		success : function(msg) {
			IPhones = jQuery.parseJSON(msg);
			
			jQuery.ajax({
				url : "queryTweets",
				type : "get",
				data : {
					"ltLat" : leafletMap.getBounds().getNorthWest().lat,
					"ltLon" : leafletMap.getBounds().getNorthWest().lng,
					"rbLat" : leafletMap.getBounds().getSouthEast().lat,
					"rbLon" : leafletMap.getBounds().getSouthEast().lng,
					"zoom" : leafletMap._zoom
							+ parseInt($('#agg-slider').jRange('getValue')),
					"decive" : 2,
					"type": 1,
					"timeLevel": 3
				},
				dataType : "text",
				success : function(msg) {
					Androids = jQuery.parseJSON(msg);
					drawCompare(IPhones, Androids)
				}
			});
		}
	});
}


function updateConfig(count, max) {

	if (count > max / 3)
		config.fillColor = '#bf444c';
	else if (count > max / 9)
		config.fillColor = '#cc6360';
	else if (count > max / 27)
		config.fillColor = '#d88273';
	else if (count > max / 81)
		config.fillColor = '#e7b98d';
	else
		config.fillColor = '#f6efa6';
	
	config.fillOpacity = 0;
	for (var i = 0; i < 10; i++) {
		config.fillOpacity += 0.4
		if (count <= Math.pow(3, i))
			break;
	}
	if (config.fillOpacity > 1)
		config.fillOpacity = 1;
	config.fillOpacity -= 0.2;
}

function wOffset(zoom) {
	w = 3600000000
	for (var i = 0; i <= zoom; i++)
		w = w / 2
	return w / 10000000.0
}

function hOffset(zoom) {
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
			$("#AggSplit").attr("value", msg)
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
			$("#AggSplit").attr("value", msg)
			$('#AggSplit').val(msg);
		}
	});
}

function drawAera(nodes){
	var count = 0;
	for (var i = 0; i < nodes.length; i++)
		count += nodes[i].z;
	areaRct.addLayer(L.rectangle([ p1, p2 ], {
		color : "#ff7800",
		weight : 1
	}).bindPopup("Count: " + count));
}

function drawMap(nodes) {
	var wO = wOffset(leafletMap._zoom
			+ parseInt($('#agg-slider').jRange('getValue')));
	var hO = hOffset(leafletMap._zoom
			+ parseInt($('#agg-slider').jRange('getValue')));
	var maxCount = 0
	for (var i = 0; i < nodes.length; i++) {
		if (maxCount < nodes[i].z)
			maxCount = nodes[i].z;
	}
	var myChart = echarts.init(document.getElementById('heat-slider'));
	myChart.setOption({
		visualMap : {
			min : 0,
			max : maxCount,
			type : 'piecewise',
			orient : 'horizontal',
			textStyle : {
				color : '#000'
			}
		}
	})
	for (var i = 0; i < nodes.length; i++) {
		var node = nodes[i];
		updateConfig(node.z, maxCount);
		if ($("#PStyle").is(":checked"))
			tweets.addLayer(L.circle([ node.y, node.x ],
					(30 + node.z) * (20 - leafletMap._zoom), config)
					.bindPopup("Count:" + node.z));
		else
			tweets.addLayer(L.rectangle(
					[ [ node.y - hO, node.x - wO ],
							[ node.y + hO, node.x + wO ] ], config).bindPopup(
					"Count:" + node.z));
	}
}
$(document)
		.ready(
				function() {
					var url = 'https://api.tiles.mapbox.com/v4/{id}/{z}/{x}/{y}.png?access_token=pk.eyJ1Ijoia2FuZXdhbmciLCJhIjoiY2pwM2UxNHNkMGF1MzNwc2FtMnNhdXJsMCJ9.KZpCBtizDeltZO6JhGc6_w';
					tweets = L.layerGroup();
					areaRct = L.layerGroup();
					overlayMaps = {
						"Android" : tweets,
						"IPhone" : areaRct
					};
					// map
					var blackMap = L
							.tileLayer(
									'https://api.mapbox.com/styles/v1/{id}/tiles/256/{z}/{x}/{y}?access_token={accessToken}',
									{
										attribution:'Linkube',
										// attribution : 'Map data &copy; <a
										// href="https://www.openstreetmap.org/">OpenStreetMap</a>
										// contributors, <a
										// href="https://creativecommons.org/licenses/by-sa/2.0/">CC-BY-SA</a>,
										// Imagery Â© <a
										// href="https://www.mapbox.com/">Mapbox</a>',
										maxZoom : 18,
										id : 'mapbox/dark-v9',
										accessToken : 'pk.eyJ1IjoiY29va2llcyIsImEiOiJjaW9sOGpwYjgwMGJtdmtqYmFieGYwcGR5In0.ot-rN7HEza9xJSijmrAOUQ'
									});
					var streatMap = L
							.tileLayer(
									'https://api.mapbox.com/styles/v1/{id}/tiles/256/{z}/{x}/{y}?access_token={accessToken}',
									{
										attribution:'Linkube',
										// attribution : 'Map data &copy; <a
										// href="https://www.openstreetmap.org/">OpenStreetMap</a>
										// contributors, <a
										// href="https://creativecommons.org/licenses/by-sa/2.0/">CC-BY-SA</a>,
										// Imagery Â© <a
										// href="https://www.mapbox.com/">Mapbox</a>',
										maxZoom : 18,
										id : 'mapbox/light-v8',
										accessToken : 'pk.eyJ1IjoiY29va2llcyIsImEiOiJjaW9sOGpwYjgwMGJtdmtqYmFieGYwcGR5In0.ot-rN7HEza9xJSijmrAOUQ'
									});
					var baseMaps = {
						"BlackMap" : blackMap,
						"LightMap" : streatMap
					}
					leafletMap = L.map('mapDiv', {
						center : [ 39.774769, -99.755859 ],
						zoom : 5,
						layers : [ blackMap, tweets, areaRct ]
					});

					var layerControl = L.control.layers(baseMaps, overlayMaps)
							.addTo(leafletMap);

					var zoom = 20 - leafletMap._zoom
					config = {
						color : 'blue',
						fillColor : '#f03',
						weight : 0,
						fillOpacity : 1
					};
					popup = L.popup();
					leafletMap.on('click', onMapClick);
					leafletMap.on('moveend', queryGet);

					$('#agg-slider').jRange({
						from : 2,
						to : 7,
						step : 1,
						format : '%s',
						scale : [ 2, 3, 4, 5, 6, 7 ],
						width : 300,
						showLabels : true,
						snap : true
					});

					myChart = echarts.init(document
							.getElementById('heat-slider'));
					option = {
						visualMap : {
							min : 0,
							max : 10000,
							type : 'piecewise',
							orient : 'horizontal',
							top : '0',
							left : '500px',
							textStyle : {
								color : '#000'
							}
						}
					};

					$("#PStyle").change(function() {
						if (last_nodes == null)
							return;
						tweets.clearLayers();
						drawMap(last_nodes);
					});

					// 使用刚指定的配置项和数据显示图表。
					myChart.setOption(option);
					t = 100
				});