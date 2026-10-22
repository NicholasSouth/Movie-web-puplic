(function () {
    "use strict";

    function cssVar(name) {
        return getComputedStyle(document.documentElement).getPropertyValue(name).trim();
    }

    function formatNumber(value) {
        return new Intl.NumberFormat("en-US", { maximumFractionDigits: 0 }).format(value || 0);
    }

    function compactMoney(value) {
        var abs = Math.abs(value);
        if (abs >= 1000000000) return (value / 1000000000).toFixed(1) + "B";
        if (abs >= 1000000) return (value / 1000000).toFixed(1) + "M";
        if (abs >= 1000) return (value / 1000).toFixed(0) + "K";
        return formatNumber(value);
    }

    /* Line chart. All colors come from the theme's CSS variables (no hard-coded colors). */
    function drawChart(canvasId, emptyId, values, labels, options) {
        var canvas = document.getElementById(canvasId);
        var empty = document.getElementById(emptyId);
        if (!canvas || !empty) return;

        var hasData = values.some(function (value) { return Number(value) > 0; });
        if (!values.length || !hasData) {
            canvas.style.display = "none";
            empty.hidden = false;
            return;
        }
        canvas.style.display = "block";
        empty.hidden = true;

        function render() {
            var rect = canvas.getBoundingClientRect();
            var ratio = window.devicePixelRatio || 1;
            var width = Math.max(280, rect.width);
            var height = Math.max(220, rect.height);
            canvas.width = Math.floor(width * ratio);
            canvas.height = Math.floor(height * ratio);
            var ctx = canvas.getContext("2d");
            ctx.setTransform(ratio, 0, 0, ratio, 0, 0);
            ctx.clearRect(0, 0, width, height);

            var gridColor = cssVar("--border");
            var labelColor = cssVar("--text-secondary");
            var lineColor = cssVar(options.colorVar);
            var pointRing = cssVar("--surface");
            var left = options.money ? 68 : 42;
            var right = 14;
            var top = 18;
            var bottom = 38;
            var plotWidth = width - left - right;
            var plotHeight = height - top - bottom;
            var maxValue = Math.max.apply(null, values.map(Number).concat([1]));
            var magnitude = Math.pow(10, Math.floor(Math.log10(maxValue)));
            var step = Math.max(1, Math.ceil(maxValue / magnitude / 4) * magnitude);
            var axisMax = Math.ceil(maxValue / step) * step;
            if (axisMax <= 0) axisMax = 1;

            ctx.font = "11px monospace";
            ctx.textBaseline = "middle";
            ctx.lineWidth = 1;
            for (var tick = 0; tick <= 4; tick++) {
                var y = top + plotHeight - (plotHeight * tick / 4);
                ctx.strokeStyle = gridColor;
                ctx.beginPath();
                ctx.moveTo(left, y);
                ctx.lineTo(width - right, y);
                ctx.stroke();
                var tickValue = axisMax * tick / 4;
                ctx.fillStyle = labelColor;
                ctx.textAlign = "right";
                ctx.fillText(options.money ? compactMoney(tickValue) : formatNumber(tickValue), left - 8, y);
            }

            var points = values.map(function (value, index) {
                var x = values.length === 1
                    ? left + plotWidth / 2
                    : left + (plotWidth * index / (values.length - 1));
                var py = top + plotHeight - (Number(value) / axisMax * plotHeight);
                return { x: x, y: py };
            });

            if (options.area && points.length > 1) {
                ctx.save();
                ctx.globalAlpha = 0.15;
                ctx.beginPath();
                ctx.moveTo(points[0].x, top + plotHeight);
                points.forEach(function (point) { ctx.lineTo(point.x, point.y); });
                ctx.lineTo(points[points.length - 1].x, top + plotHeight);
                ctx.closePath();
                ctx.fillStyle = lineColor;
                ctx.fill();
                ctx.restore();
            }

            ctx.beginPath();
            points.forEach(function (point, index) {
                if (index === 0) ctx.moveTo(point.x, point.y);
                else ctx.lineTo(point.x, point.y);
            });
            ctx.strokeStyle = lineColor;
            ctx.lineWidth = 2.5;
            ctx.lineJoin = "round";
            ctx.lineCap = "round";
            ctx.stroke();

            /* Dots only for short ranges; long ranges would turn into a dotted line. */
            if (points.length <= 31) {
                points.forEach(function (point) {
                    ctx.beginPath();
                    ctx.arc(point.x, point.y, 3.5, 0, Math.PI * 2);
                    ctx.fillStyle = lineColor;
                    ctx.fill();
                    ctx.strokeStyle = pointRing;
                    ctx.lineWidth = 2;
                    ctx.stroke();
                });
            }

            var maxLabels = Math.min(labels.length, Math.max(2, Math.floor(plotWidth / 60)));
            var labelIndices = [];
            if (labels.length <= maxLabels) {
                for (var i = 0; i < labels.length; i++) labelIndices.push(i);
            } else {
                for (var j = 0; j < maxLabels; j++) {
                    labelIndices.push(Math.round(j * (labels.length - 1) / (maxLabels - 1)));
                }
            }
            ctx.fillStyle = labelColor;
            ctx.textAlign = "center";
            ctx.textBaseline = "top";
            labelIndices.forEach(function (index) {
                if (points[index]) ctx.fillText(labels[index], points[index].x, top + plotHeight + 12);
            });
        }

        render();
        if (window.ResizeObserver) {
            new ResizeObserver(render).observe(canvas.parentElement);
        } else {
            window.addEventListener("resize", render);
        }
    }

    document.addEventListener("DOMContentLoaded", function () {
        var data = window.adminStatisticsData;
        if (!data) return;
        drawChart("revenueChart", "revenueChartEmpty", data.revenue || [], data.labels || [],
            { money: true, area: true, colorVar: "--accent" });
        drawChart("bookingChart", "bookingChartEmpty", data.bookings || [], data.labels || [],
            { money: false, area: false, colorVar: "--success" });
    });
}());
