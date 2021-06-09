var base = +new Date(2016, 9, 3);
var oneDay = 24 * 3600 * 1000;
var valueBase = Math.random() * 300;
var valueBase2 = Math.random() * 300;
compaeData = [];
compaeData2 = [];

for (var i = 1; i < 10; i++) {
    var now = new Date(base += oneDay);
    var dayStr = [now.getFullYear(), now.getMonth() + 1, now.getDate()].join('-');

    valueBase = Math.round((Math.random() - 0.5) * 20 + valueBase);
    valueBase <= 0 && (valueBase = Math.random() * 300);
    compaeData.push([dayStr, valueBase]);

    valueBase2 = Math.round((Math.random() - 0.5) * 20 + valueBase2);
    valueBase2 <= 0 && (valueBase2 = Math.random() * 300);
    compaeData2.push([dayStr, valueBase2]);
}

function drawCompare(IPhone, Android) {
	compaeData = [];
	compaeData2 = [];
	var timeChart = echarts.init(document.getElementById('time_zoom'));
	var opt = timeChart.getOption();
	var start = categoryData[opt.dataZoom[0].startValue];
	var end = categoryData[opt.dataZoom[0].endValue]
	for (var i = 0; i < Math.min(IPhone.length, Android.length); i++){
		if (start <= IPhone[i].x && IPhone[i].x <= end)
			compaeData.push([[IPhone[i].x / 10000 | 0, IPhone[i].x % 10000 / 100 | 0, IPhone[i].x % 100].join('-'), IPhone[i].y]);
		if (start <= Android[i].x && Android[i].x <= end)
			compaeData2.push([[Android[i].x / 10000 | 0, Android[i].x % 10000 / 100 | 0, Android[i].x % 100].join('-'), Android[i].y]);
	}
	compare_option.series[0].data = compaeData
	compare_option.series[1].data = compaeData2
	var compareChart = echarts.init(document.getElementById('compare'));
	compareChart.setOption(compare_option);
}

compare_option = {
    animation: false,
    legend: {
        data: ['IPhone','Android']
    },
    tooltip: {
        triggerOn: 'none',
        position: function (pt) {
            return [pt[0], 130];
        }
    },
    xAxis: {
        type: 'time',
        // boundaryGap: [0, 0],
        axisPointer: {
            value: '2016-10-7',
            snap: true,
            lineStyle: {
                color: '#004E52',
                opacity: 0.5,
                width: 2
            },
            label: {
                show: true,
                formatter: function (params) {
                    return echarts.format.formatTime('yyyy-MM-dd', params.value);
                },
                backgroundColor: '#004E52'
            },
            handle: {
                show: true,
                color: '#004E52'
            }
        },
        splitLine: {
            show: false
        }
    },
    yAxis: {
        type: 'value',
        axisTick: {
            inside: true
        },
        splitLine: {
            show: false
        },
        axisLabel: {
            inside: true,
            formatter: '{value}\n'
        },
        z: 10
    },
    grid: {
        top: 110,
        left: 15,
        right: 15,
        height: 160
    },
    series: [
        {
            name: 'IPhone',
            type: 'line',
            smooth: true,
            symbol: 'circle',
            symbolSize: 5,
            sampling: 'average',
            itemStyle: {
                color: '#8ec6ad'
            },
            stack: 'a',
            areaStyle: {
                color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [{
                    offset: 0,
                    color: '#8ec6ad'
                }, {
                    offset: 1,
                    color: '#ffe'
                }])
            },
            data: compaeData
        },
        {
            name: 'Android',
            type: 'line',
            smooth: true,
            stack: 'a',
            symbol: 'circle',
            symbolSize: 5,
            sampling: 'average',
            itemStyle: {
                color: '#d68262'
            },
            areaStyle: {
                color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [{
                    offset: 0,
                    color: '#d68262'
                }, {
                    offset: 1,
                    color: '#ffe'
                }])
            },
            data: compaeData2
        }
    ]
};

var compareChart = echarts.init(document.getElementById('compare'));
compareChart.setOption(compare_option);
