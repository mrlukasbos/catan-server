var json = null;

function draw() {
    d3.select("svg").remove();

var tiles = json.attributes.tiles,
    edges = json.attributes.edges,
    nodes = json.attributes.nodes,
    players = json.attributes.players,
    bandits = json.attributes.bandits;

var width = 1050,
    height = 900,
    radius = 75;

var topology = hexTopology(radius, width, height);

var projection = hexProjection(radius);

var path = d3.geo.path()
    .projection(projection);

var svg = d3.select("#d3-holder").append("svg")
    .attr("width", width)
    .attr("height", height);

svg.append("g")
    .attr("class", "hexagon")
    .selectAll("path")
    .data(topology.objects.hexagons.geometries)
    .enter().append("path")
    .attr("d", function (d) {
        return path(topojson.feature(topology, d));
    })
    .attr("class", function (d) {
        return d.tile.attributes.type;
    });

svg.append("g")
    .attr("class", "numbers")
    .selectAll("path")
    .data(topology.objects.hexagons.geometries)
    .enter().append("text")
    .attr("x", function (d) {
        return path.centroid(topojson.feature(topology, d))[0];
    })
    .attr("y", function (d) {
        return path.centroid(topojson.feature(topology, d))[1] - 20;
    })
    .text(function (d) {
        if (d.tile.attributes.number > 0) return d.tile.attributes.number;
        return "";
    })
    .attr("text-anchor", "middle");

svg.append("g")
    .attr("class", "labels")
    .selectAll("path")
    .data(topology.objects.hexagons.geometries)
    .enter().append("text")
    .attr("x", function (d) {
        return path.centroid(topojson.feature(topology, d))[0];
    })
    .attr("y", function (d) {
        return path.centroid(topojson.feature(topology, d))[1];
    })
    .text(function (d) {
        return d.tile.key;
    })
    .attr("text-anchor", "middle");

svg.append("g")
    .attr("class", "types")
    .selectAll("path")
    .data(topology.objects.hexagons.geometries)
    .enter().append("text")
    .attr("x", function (d) {
        return path.centroid(topojson.feature(topology, d))[0];
    })
    .attr("y", function (d) {
        return path.centroid(topojson.feature(topology, d))[1] + 15;
    })
    .text(function (d) {
        return d.tile.attributes.type;
    })
    .attr("text-anchor", "middle");

svg.append("path")
    .datum(topojson.mesh(topology, topology.objects.hexagons))
    .attr("class", "mesh")
    .attr("d", path);

for (var i = 0; i < players.length; i++) {
    var player = players[i];

    var border = svg.append("path")
        .attr("class", "border")
        .attr("stroke", player.attributes.color)
        .call(redraw);

    function redraw(border) {
        border.attr("d", path(topojson.mesh(topology, topology.objects.hexagons, function (a, b) {
            var edge1 = getEdge(a.tile.key, b.tile.key);
            var edge2 = getEdge(b.tile.key, a.tile.key);

            if (edge1 && edge1.attributes.player == player.id) {
                return edge1.attributes.road;
            } else if (edge2 && edge2.attributes.player == player.id) {
                return edge2.attributes.road;
            }
            return false;
        })));
    }
}

svg.append("g")
    .attr("class", "nodes")
    .selectAll("path")
    .data(nodes)
    .enter().append("path")
    .attr("transform", function (d) {
        var coordinate = path.centroid(topojson.merge(topology, [
            getHexByKey(d.attributes.t_key),
            getHexByKey(d.attributes.l_key),
            getHexByKey(d.attributes.r_key)
        ]));
        return "translate(" + coordinate[0] + "," + coordinate[1] + ")";
    })
    .attr("d", function (d) {
        if (d.attributes.structure == "settlement") {
            return "M0 -10 L10 -2 L10 10 L-10 10 L-10 -2 L0 -10 Z"
        } else if (d.attributes.structure == "city") {
            return "M-12 -2 L1 -2 L1 -10 L6 -12 L11 -10 L11 12 L-12 12 L-12 -2 Z"
        }
    }).attr('fill', function (d) {
    return d.attributes.player_color;
    }).attr('stroke', '#000000');


svg.append("g")
    .attr("class", "bandits")
    .selectAll("path")
    .data(bandits)
    .enter().append("path")
    .attr("transform", function (d) {
        var coordinate = path.centroid(topojson.feature(topology, getHexByKey(d.attributes.tile_key)));
        return "translate(" + coordinate[0] + "," + coordinate[1] + ")";
    })
    .attr("d", "M-10 35 m -5, 0 a 10,10 0 1,0 30,0 a 10,10 0 1,0 -30,0");

function hexTopology(radius, width, height) {
    var dx = radius * 2 * Math.sin(Math.PI / 3),
        dy = radius * 1.5,
        m = Math.ceil((height + radius) / dy) + 1,
        n = Math.ceil(width / dx) + 1,
        geometries = [],
        arcs = [];

    for (var j = -1; j <= m; ++j) {
        for (var i = -1; i <= n; ++i) {
            var y = j * 2, x = (i + (j & 1) / 2) * 2;
            arcs.push([[x, y - 1], [1, 1]], [[x + 1, y], [0, 1]], [[x + 1, y + 1], [-1, 1]]);
        }
    }

    for (var j = 0, q = 3; j < m; ++j, q += 6) {
        for (var i = 0; i < n; ++i, q += 3) {
            if (getTile(i - 1, j - 2)) {
                geometries.push({
                    type: "Polygon",
                    arcs: [[q, q + 1, q + 2, ~(q + (n + 2 - (j & 1)) * 3), ~(q - 2), ~(q - (n + 2 + (j & 1)) * 3 + 2)]],
                    tile: getTile(i - 1, j - 2)
                });
            }
        }
    }

    return {
        transform: {translate: [0, 0], scale: [1, 1]},
        objects: {hexagons: {type: "GeometryCollection", geometries: geometries}},
        arcs: arcs
    };
}

function getTile(x, y) {
    var key = `[${x},${y}]`;
    return tiles.find(matchKey, key);
}

function getEdge(a, b) {
    var key = `(${a},${b})`;
    return edges.find(matchKey, key);
}

function matchKey(item) {
    return item.key == this;
}

function getHexByKey(key) {
    return topology.objects.hexagons.geometries.find(matchTile, key)
}

function matchTile(item) {
    return item.tile.key == this;
}

function hexProjection(radius) {
    var dx = radius * 2 * Math.sin(Math.PI / 3),
        dy = radius * 1.5;
    return {
        stream: function (stream) {
            return {
                point: function (x, y) {
                    stream.point(x * dx / 2, (y - (2 - (y & 1)) / 3) * dy / 2);
                },
                lineStart: function () {
                    stream.lineStart();
                },
                lineEnd: function () {
                    stream.lineEnd();
                },
                polygonStart: function () {
                    stream.polygonStart();
                },
                polygonEnd: function () {
                    stream.polygonEnd();
                }
            };
        }
    };
}
}

var app = new Vue({
    el: '#app',
    data: {
        ip: 'localhost',
        port: '10007',
        status: 'NOT_CONNECTED',
        json: '',
        socket: null,
        systemMsg: '',
        gameRunning: false,
        players: []
    },
    methods: {
        connect: function (event) {
            this.json = null;
            this.status = 'CONNECTING';
            this.killSocket();

            this.socket = new WebSocket("ws:" + this.ip + ":" + this.port);

            this.socket.onerror = (err) => {
                this.status = "CONNECTION_FAILURE";
            }

            this.socket.onclose = () => {
                this.killSocket();
            }

            this.socket.onopen = () => {
                this.status = "CONNECTED";
            }

            this.socket.onmessage = (data) => {

                // get the first three characters of the new data
                // it should indicate what to do with the data
                var identifier = data.data.toString().substring(0, 3);
                var message = data.data.toString().substring(3, data.data.length);

                switch (identifier) {
                    case "BOA": {
                        json = JSON.parse(message);
                        draw();
                        break;
                    }
                    case "COM": {
                        this.systemMsg = message;
                        break;
                    }
                    case "GRU": {
                       // console.log(message);
                        this.gameRunning = message === "true";
                        break;
                    }
                    case "PLA": {
                        console.log(message)
                        this.players = JSON.parse(message);
                    }
                }
            }
        },
        startGame: function(event) {
            if (this.socket && (!this.gameRunning || confirm("Are you sure you want to start?"))) {
                this.socket.send("START");
            }
        },
        endGame: function(event) {
            if (this.socket && (this.gameRunning || confirm("Are you sure you want to stop?"))) {
                this.socket.send("END");
            }
        },
        killSocket: function () {
            if (this.socket) {
                this.socket.close();
                delete this.socket;
            }
        }
    },
    mounted: function() {
        if (localStorage.ip) {
            this.ip = localStorage.ip;
        }
        if (localStorage.port) {
            this.port = localStorage.port;
        }
        this.connect();
    }
});