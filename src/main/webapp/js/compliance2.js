var checked = false;
$(document).ready(
    function() {
        var getColor = function() {
            var counter = 1;
            return function() {
                counter++;
                return (counter % 2) === 0 ? "white" : "";
            }
        };

        var getIndex = function() {
            var counter = 1;
            var index = 0
            return function() {
                counter++;
                if ((counter % 2) === 0) {
                    index++;
                }
                return index;
            }
        };

        var toggleWire = function(event) {
            if ($(event.currentTarget).find(".arrow").hasClass('icon-chevron-right')) {
                $(event.currentTarget).find(".arrow").removeClass("icon-chevron-right");
                $(event.currentTarget).find(".arrow").addClass("icon-chevron-down");
                $("#" + $(event.currentTarget).attr("index")).removeClass("hidden");
                $("#" + $(event.currentTarget).attr("index")).slideDown("slow");
            } else {
                $(event.currentTarget).find(".arrow").removeClass("icon-chevron-down");
                $(event.currentTarget).find(".arrow").addClass("icon-chevron-right");
                $("#" + $(event.currentTarget).attr("index")).slideUp("slow");
            }
        };

        var handleError = function(event) {
            $("#spinner-container2").empty();
            $("#compliance-error2").text("An error occurred. " + event.statusText);
            $("#compliance-error-container2").show();
        };

        var handleResponse = function(data) {

            $("#spinner-container2").empty();

            if (data.authRequired === "true") {
                var template = $('#authMethodTemplate2').html();
                var html = Mustache.to_html(template, data);
                $("#authMethod2").html = html;

                $("#authenticationAlert2").show();
                $("#settingsArrow2").removeClass("icon-chevron-right");
                $("#settingsArrow2").addClass("icon-chevron-down");
                $("#settings2").slideDown();
            } else {
                data.index = getIndex();
                data.color = getColor();
                var template = $('#testTemplate').html();
                var html = Mustache.to_html(template, data);
                $("#compliance-result-container2").html(html);
                $(".label-info").click(toggleWire);
                $("[rel=tooltip]").tooltip();
                var success = parseInt(data.statistics.success);
                var failed = parseInt(data.statistics.failed);
                var skipped = parseInt(data.statistics.skipped);
                template = $('#statisticsTemplate').html();
                html = Mustache.to_html(template, {
                    total : failed + success,
                    success : success,
                    failed : failed,
                    skipped : skipped
                });
                $("#compliance-statistics-text2").html(html);

                var options = {
                    legend : 'none',
                    chartArea : {
                        width : "100%",
                        height : "100%"
                    },
                    colors : [ "#f2dede", "#dff0d8", "#d9edf7"  ],
                    backgroundColor : "whiteSmoke",
                    pieSliceTextStyle : {color:"#999999"}
                };

                var chartData = google.visualization.arrayToDataTable([
                    [ 'Result', 'Number' ], [ 'Failed', failed ],
                    [ 'Success', success ], [ 'Skipped', skipped ] ]);
                new google.visualization.PieChart(document
                    .getElementById('compliance-chart2')).draw(chartData, options);

                prettyPrint();
                $("#result-container2").show();
            }
        };

        var authMethodChanged = function() {
            $("#authMethod2 option:selected").each(function() {
                $(".authMethod2").hide();
                $("." + $(this).val()).show();
            });
        };

        var sendRequest2 = function() {
            if (!checked) {
                var data = {
                    url: $("#complianceUrl2").val(),
                    authMethod: $("#authMethod2 option:selected").val(),
                    username: $("#username2").val(),
                    password: $("#password2").val(),
                    clientId: $("#oauthClientId2").val(),
                    clientSecret: $("#oauthClientSecret2").val(),
                    authorizationServer: $("#oauthAuthorizationServer2").val(),
                    authorizationHeader: $("#rawAuthorizationHeader2").val()
                };

                $("#authenticationAlert2").hide();
                $("#result-container2").hide();
                $("#compliance-error2").hide();
                $("#compliance-error-container2").hide();

                $("#spinner-container2").spin({
                    lines: 13,
                    length: 30,
                    width: 10,
                    radius: 40,
                    rotate: 0,
                    color: '#000',
                    speed: 1,
                    trail: 60,
                    shadow: false,
                    hwaccel: false,
                    className: 'spinner',
                    zIndex: 2e9,
                    top: 'auto',
                    left: 'auto'
                });
                checked = true;
                $.post("/compliance2/test2", data, handleResponse).error(handleError);
                return false;
            }
        };
        $("#toggleAddImplementation2").click(function(){toggleWire({currentTarget:$("#toggleAddImplementation2")})});
        $("#toggleSettings2").click(function(){toggleWire({currentTarget:$("#toggleSettings2")})});
        $("#sendCompliance2").click(sendRequest2);
        $("#authMethod2").change(authMethodChanged).change();
    });