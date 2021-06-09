
categoryData = [];
barData = [];
for (var i = 0; i < 200; i++) {
    var val = Math.random() * 1000;
    categoryData.push('category' + i);
    barData.push(echarts.number.round(val, 2));
}

function drawTimeZoom(nodes) {
	categoryData = []
	barData = []
	for (var i = 0; i < nodes.length; i++){
		categoryData.push(nodes[i].x);
		barData.push(nodes[i].y);
	}
	var timeChart = echarts.init(document.getElementById('time_zoom'));
	var opt = timeChart.getOption();
	var _option = {
	    tooltip: {
	        trigger: 'axis',
	        axisPointer: {
	            type: 'shadow'
	        }
	    },
	    legend: {
	        data: ['Count']
	    },
	    dataZoom: [{
	        type: 'slider',
	        start: opt.dataZoom[0].start,
	        end: opt.dataZoom[0].end
	    }, {
	        type: 'inside',
	        start: 50,
	        end: 70
	    }],
	    xAxis: {
	        data: categoryData
	    },
	    yAxis: {},
	    series: [{
	        type: 'bar',
	        name: 'Count',
	        data: barData,
	        itemStyle: {
	            color: '#77bef7'
	        }
	    }]
	};
	timeChart.setOption(_option);
}

var timeZoom_option = {
    tooltip: {
        trigger: 'axis',
        axisPointer: {
            type: 'shadow'
        }
    },
    legend: {
        data: ['Count']
    },
    dataZoom: [{
        type: 'slider',
        start: 0,
        end: 100
    }, {
        type: 'inside',
        start: 50,
        end: 70
    }],
    xAxis: {
        data: categoryData
    },
    yAxis: {},
    series: [{
        type: 'bar',
        name: 'Count',
        data: barData,
        itemStyle: {
            color: '#77bef7'
        }
    }]
};


var timeChart = echarts.init(document.getElementById('time_zoom'));
timeChart.setOption(timeZoom_option);

function tzMouseUp(){
	console.info(123);
	queryGet();
}

