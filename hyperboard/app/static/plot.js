// number of x axis ticks
var NUM_XTICKS = 6;

// set the dimensions and margins of the graph
var margin = {top: 50, right: 30, bottom: 30, left: 80},
    width = 600 - margin.left - margin.right,
    height = 500 - margin.top - margin.bottom;

// append the svg object to the body of the page
var svg = d3.select("#history_plots")
  .append("svg")
    .attr("width", width + margin.left + margin.right)
    .attr("height", height + margin.top + margin.bottom)
  .append("g")
    .attr("transform",
          "translate(" + margin.left + "," + margin.top + ")");

function plot_data(data, xfn, xlabel, yfn, ylabel) {
  svg.append("text")
        .attr("x", (width / 2))
        .attr("y", 0 - (margin.top / 2))
        .attr("text-anchor", "middle")
        .text(xlabel + " v. " + ylabel);

  // Add X axis --> it is a date format
  var x = d3.scaleTime()
    .domain(d3.extent(data, xfn))
    .range([ 0, width ]);

  svg.append("g")
    .attr("transform", "translate(0," + height + ")")
    .call(d3.axisBottom(x)
        .ticks(NUM_XTICKS));

  // Add Y axis
  var y = d3.scaleLinear()
    .domain([0, d3.max(data, yfn)])
    .range([ height, 0 ]);
  svg.append("g")
    .call(d3.axisLeft(y));

    // text label for the x axis
  svg.append("text")
      .attr("transform",
            "translate(" + (width/2) + " ," +
                           (height + margin.top + 20) + ")")
      .style("text-anchor", "middle")
      .text("Date");

  // Add the line
  svg.append("path")
    .datum(data)
    .attr("fill", "none")
    .attr("stroke", "steelblue")
    .attr("stroke-width", 1.5)
    .attr("d", d3.line()
      .x(function(d) { return x(xfn(d)); })
      .y(function(d) { return y(yfn(d)); })
      .curve(d3.curveStepAfter)
    );
}





function getfolder(e) {
  var files = e.target.files;
  var path = files[0].webkitRelativePath;
  var Folder = path.split("/");
  alert(Folder[0]);
}
