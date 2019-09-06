<%@ page import="grails.util.Holders; java.util.Locale" %>
<?xml version="1.0" encoding="UTF-8"?>
<html lang="en">
<head>
    <!-- Include default page title -->
    <title><g:layoutTitle default="OpenBoxes" /></title>

    <!-- YUI -->
    <yui:stylesheet dir="reset-fonts-grids" file="reset-fonts-grids.css" />

    <!-- Include Favicon -->
    <link rel="shortcut icon" href="${createLinkTo(dir:'images',file:'favicon.ico')}" type="image/x-icon" />

    <!-- Include Main CSS -->
    <link rel="stylesheet" href="${createLinkTo(dir:'js/jquery.megaMenu/',file:'jquery.megamenu.css')}" type="text/css" media="all" />
    <link rel="stylesheet" href="${createLinkTo(dir:'js/jquery.nailthumb',file:'jquery.nailthumb.1.1.css')}" type="text/css" media="all" />
    <link rel="stylesheet" href="${createLinkTo(dir:'js/chosen',file:'chosen.css')}" type="text/css" media="all" />
    <link rel="stylesheet" href="//cdnjs.cloudflare.com/ajax/libs/datatables/1.9.4/css/jquery.dataTables.min.css" type="text/css">
    <link rel="stylesheet" href="${createLinkTo(dir:'css',file:'footable.css')}" type="text/css" media="all" />

    <%--<link rel="stylesheet" href="${createLinkTo(dir:'js/feedback',file:'feedback.css')}" type="text/css" media="all" />--%>
    <!-- Include javascript files -->
    <g:javascript library="application"/>

    <!-- Include jQuery UI files -->
    <g:javascript library="jquery" plugin="jquery" />
    <jqui:resources />
    <link href="${createLinkTo(dir:'js/jquery.ui/css/smoothness', file:'jquery-ui.css')}" type="text/css" rel="stylesheet" media="screen, projection" />


    <!-- Include Jquery Validation and Jquery Validation UI plugins -->
    <jqval:resources />
    <jqvalui:resources />

    <link rel="stylesheet" href="${resource(dir:'css',file:'openboxes.css')}" type="text/css" media="all" />
    <link rel="stylesheet" href="${resource(dir:'css',file:'grids.css')}" type="text/css" media="all" />
    <link rel="stylesheet" href="${resource(dir:'css',file:'loading.css')}" type="text/css" media="all" />

    <!-- jquery validation messages -->
    <g:if test="${ session?.user?.locale && session?.user?.locale != 'en'}">
        <script src="${createLinkTo(dir:'js/jquery.validation/', file:'messages_'+ session?.user?.locale + '.js')}"  type="text/javascript" ></script>
    </g:if>

    <!-- Grails Layout : write head element for page-->
    <g:layoutHead />

    <g:render template="/common/customCss"/>
    <g:render template="/common/fullstory"/>
    <g:render template="/common/hotjar"/>

    <ga:trackPageview />
    <r:layoutResources/>
</head>
<body class="yui-skin-sam">

<g:render template="/common/customVariables"/>
<div id="doc3">

    <g:if test="${grails.util.Holders.config.openboxes.system.notification.enabled}">
        <div class="notice">
            ${Holders.config.openboxes.system.notification.message}
        </div>
    </g:if>
    <g:if test="${session.impersonateUserId}">
        <div class="notice">
            <g:message code="user.impersonate.message" args="[session.user.username]" default="You are impersonating user {0}."/>
            <g:link controller="auth" action="logout">
                ${g.message(code:'default.logout.label', default: "Logout")}
            </g:link>
        </div>
    </g:if>
    <g:if test="${session.useDebugLocale}">

        <div id="debug-header" class="notice" style="margin: 10px;">
            <warehouse:message code="localization.custom.message"/>
            <g:link controller="localization" action="list" class="button">
                <warehouse:message code="default.list.label" args="[message(code: 'localizations.label')]"/>
            </g:link>
            <g:link controller="localization" action="create" class="button">
                <warehouse:message code="default.add.label" args="[message(code: 'localization.label')]"/>
            </g:link>
            <div class="right">
                <g:link controller="user" action="disableLocalizationMode" class="button">
                    <warehouse:message code="localization.disable.label"/>
                </g:link>
            </div>
            <div id="localizations">
            <!--
                        At some point we may want to display all translations for the page in a single div.
                        For the time being, flash.localizations is empty.
                     -->
                <g:each var="localization" in="${flash.localizations }">
                    <div>
                        ${localization.code } = ${localization.text }
                    </div>
                </g:each>
            </div>
        </div>
    </g:if>

    <!-- Header "hd" includes includes logo, global navigation -->
    <g:if test="${session?.user && session?.warehouse}">
        <div id="hd" role="banner">
            <g:render template="/common/header"/>
        </div>
        <div id="megamenu">
            <g:include controller="dashboard" action="megamenu" params="[locationId:session?.warehouse?.id,userId:session?.user?.id]"/>
            <div id="loader" style="display:none; position: absolute; right: 0; top: 0" class="right notice">
                ${g.message(code: 'default.loading.label')}
            </div>

        </div>
        <div id="breadcrumb">
            <g:render template="/common/breadcrumb"/>
        </div>
        <%--
        <div class="box center" style="margin:0;">
            <g:globalSearch id="globalSearch" cssClass="globalSearch" name="searchTerms"
                            jsonUrl="${request.contextPath }/json/globalSearch"></g:globalSearch>
        </div>
        --%>
    </g:if>


<!-- Body includes the divs for the main body content and left navigation menu -->
    <div id="bd" role="main">
        <div id="yui-main">
            <div id="content" class="yui-b">
                <g:layoutBody />
            </div>
        </div>
    </div>

    <g:if test="${session.useDebugLocale}">
        <g:render template="/common/localization"/>
    </g:if>


<!-- YUI "footer" block that includes footer information -->
</div>
<div id="dlgShowDialog" class="dialog hidden">
    <div id="dlgShowDialogContent" class="empty center">
        Loading ...
    </div>
</div>
<!-- Include other plugins -->
<script src="${createLinkTo(dir:'js/jquery.ui/js/', file:'jquery.ui.autocomplete.selectFirst.js')}" type="text/javascript" ></script>
<script src="${createLinkTo(dir:'js/jquery.cookies/', file:'jquery.cookies.2.2.0.min.js')}" type="text/javascript" ></script>
<script src="${createLinkTo(dir:'js/jquery.cookie/', file:'jquery.cookie.js')}" type="text/javascript" ></script>
<script src="${createLinkTo(dir:'js/jquery.tmpl/', file:'jquery.tmpl.js')}" type="text/javascript" ></script>
<script src="${createLinkTo(dir:'js/jquery.tmplPlus/', file:'jquery.tmplPlus.js')}" type="text/javascript" ></script>
<script src="${createLinkTo(dir:'js/jquery.livequery/', file:'jquery.livequery.min.js')}" type="text/javascript" ></script>
<script src="${createLinkTo(dir:'js/jquery.livesearch/', file:'jquery.livesearch.js')}" type="text/javascript" ></script>
<script src="${createLinkTo(dir:'js/jquery.hoverIntent/', file:'jquery.hoverIntent.minified.js')}" type="text/javascript" ></script>
<script src="${createLinkTo(dir:'js/knockout/', file:'knockout-2.2.0.js')}" type="text/javascript"></script>
<script src="${createLinkTo(dir:'js/', file:'knockout_binding.js')}" type="text/javascript"></script>
<script src="${createLinkTo(dir:'js/jquery.nailthumb', file:'jquery.nailthumb.1.1.js')}" type="text/javascript" ></script>
<g:if test="${System.getenv().get('headless') != 'false'}" env="test">
    <!--headless driver throw error when using watermark-->
</g:if>
<g:else>
    <script src="${createLinkTo(dir:'js/jquery.watermark/', file:'jquery.watermark.min.js')}" type="text/javascript" ></script>
</g:else>
<script src="${createLinkTo(dir:'js/', file:'global.js')}" type="text/javascript" ></script>
<script src="${createLinkTo(dir:'js/jquery.megaMenu/', file:'jquery.megamenu.js')}" type="text/javascript" ></script>
<script src="${createLinkTo(dir:'js/', file:'underscore-min.js')}" type="text/javascript" ></script>
<script src="${createLinkTo(dir:'js/chosen/', file:'chosen.jquery.min.js')}" type="text/javascript" ></script>
<script src="${createLinkTo(dir:'js/feedback/', file:'feedback.js')}" type="text/javascript" ></script>
<script src="//cdnjs.cloudflare.com/ajax/libs/datatables/1.9.4/jquery.dataTables.js" type="text/javascript" ></script>
<script src="${createLinkTo(dir:'js/footable/', file:'footable.js')}" type="text/javascript" ></script>
<script src="//cdnjs.cloudflare.com/ajax/libs/notify/0.4.2/notify.js" type="text/javascript"></script>

<!-- JIRA Issue Collector -->
<g:if test="${session.user && Boolean.valueOf(Holders.config.openboxes.jira.issue.collector.enabled)}">
    <script type="text/javascript" src="${Holders.config.openboxes.jira.issue.collector.url}"></script>
</g:if>

<!-- Localization -->
<g:if test="${session.useDebugLocale}">
    <script type="text/javascript">
        // Define the localization
        if(typeof openboxes === "undefined") openboxes = {};
        if(typeof openboxes.localization === "undefined") openboxes.localization = {};
        openboxes.localization.Localization = function(data) {
            console.log(data);
            var self = this;
            if(!data) data = {};
            self.id = ko.observable(data.id);
            self.code = ko.observable(data.code);
            self.locale = ko.observable(data.locale);
            self.text = ko.observable(data.text);
            self.translation = ko.observable(data.translation);
            //self.deleteUrl = ko.observable("${request.contextPath}/json/deleteLocalization?id=" + data.id);
            //self.resolvedText = ko.observablae(data.resolvedText);
            //self.lastUpdated = ko.observable(data.lastUpdated);
            //self.version = ko.observable(data.version);
        };

        $(document).ready(function() {
            // Initialize the localization dialog
            $("#localization-dialog").dialog({ autoOpen: false, modal: true, width: '600px' });


            // Instantiate a new localization object to be used
            var data = { id:"", code: "", text: "", translation: "" };
            var viewModel = new openboxes.localization.Localization(data);
            ko.applyBindings(viewModel);

            // Delete localization event handler
            $("#delete-localization-btn").click(function() {
                event.preventDefault();
                console.log("delete localization");
                console.log($(this));
                console.log($(event));
                if (viewModel.id() == undefined) {
                    alert("This translation is not currently saved to the database so it cannot be deleted.");
                }
                else {
                    $.ajax({
                        url: "${request.contextPath}/json/deleteLocalization",
                        type: "get",
                        contentType: 'text/json',
                        dataType: "json",
                        data: {id: viewModel.id() },
                        success: function(data) {
                            alert("You have successfully deleted this localization.");
                            location.reload();
                        },
                        error: function(data) {
                            alert("An error occurred while deleting this translation.");
                        }
                    });
                }

            });

            // Close dialog event handler
            $("#close-localization-dialog-btn").click(function() {
                event.preventDefault();
                $("#localization-dialog").dialog("close");
            });

            // Help event handler
            $("#help-localization-btn").click(function() {
                event.preventDefault();
                $.ajax({
                    url: "${request.contextPath}/json/getTranslation",
                    type: "get",
                    contentType: 'text/json',
                    dataType: "json",
                    data: {text: viewModel.text, src: "en", dest: "fr"},
                    success: function(data) {
                        //alert("success: " + data);
                        console.log(data);
                        viewModel.translation = data;
                        //ko.applyBindings(viewModel);
                    },
                    error: function(data) {
                        //console.log(data);
                        //alert("error");
                        viewModel.translation = "Error. Try again.";
                        //ko.applyBindings(viewModel);
                    }
                });
            });

            // Save event handler
            $("#save-localization-btn").click(function() {
                event.preventDefault();
                var jsonData = ko.toJSON(viewModel);
                console.log("save localization");
                console.log(jsonData);

                $.ajax({
                    url: "${request.contextPath}/json/saveLocalization",
                    type: "post",
                    contentType: 'text/json',
                    dataType: "json",
                    data: jsonData,
                    success: function(data) {
                        //alert("success");
                        $("#localization-dialog").dialog("close");
                        location.reload();
                    },
                    error: function(data) {
                        //alert("fail");
                        $("#localization-dialog").dialog("close");
                        location.reload();
                    }
                });
            });

            // Open dialog event handler
            $(".open-localization-dialog").click(function() {
                var id = $(this).attr("data-id");
                var code = $(this).attr("data-code");
                var resolvedMessage = $(this).attr("data-resolved-message");
                console.log("Get localization");
                console.log(id);
                console.log(code);
                var url = "${request.contextPath}/json/getLocalization";
                $.getJSON( url, { id: id, code: code, resolvedMessage: resolvedMessage },
                        function (data, status, jqxhr) {
                            console.log("getJSON response: ");
                            console.log(data);
                            viewModel.id(data.id);
                            viewModel.code(data.code);
                            viewModel.text(data.text);
                            viewModel.locale(data.locale);
                            viewModel.translation(data.translation);
                        }
                );

                $("#localization-dialog").dialog('open');
                event.preventDefault();
            });

        });
    </script>
</g:if>

<g:javascript>

    function openModalDialog(target, title, width, height, url) {

        var position = {
            my: "center center",
            at: "center center",
            of: window
        };

        $(target).attr("title", title);
        $(target).dialog({
            title: title,
            autoOpen: true,
            modal: true,
            width: width,
            autoResize:true,
            resizable: true,
            minHeight: height,
            position: position,
            open: function(event, ui) {
                $(this).html("Loading...");
                $(this).load(url, function(response, status, xhr) {
                    if (xhr.status !== 200) {
                        $(this).text("");
                        $("<p></p>").addClass("error").text("Error: " + xhr.status + " " + xhr.statusText).appendTo($(this));
                        var error = JSON.parse(response);
                        var stack = $("<div></div>").addClass("stack empty").appendTo($(this));
                        $("<code></code>").text(error.errorMessage).appendTo(stack)
                    }
                });
            }
        }).dialog('open');
    }

    $(document).ready(function() {

        $(".btn-show-dialog").live("click", function (event) {
            var url = $(this).data("url");
            var title = $(this).data("title");
            var target = $(this).data("target") || "#dlgShowDialog";
            var width = $(this).data("width") || "800";
            var height = $(this).data("height") || "auto";
            openModalDialog(target, title, width, height, url)
        });

        $(".btn-close-dialog").live("click", function (event) {
            event.preventDefault();
            var target = $(this).data("target") || "#dlgShowDialog";
            $(target).dialog( "close" );
        });

	});


</g:javascript>
<script type="text/javascript">
    $(document).ready(function() {

        //Feedback({
        //    h2cPath: "${createLinkTo(dir:'js/html2canvas/', file:'html2canvas.js')}",
        //    url: '${createLink(controller: 'errors', action: 'sendFeedback')}',
        //    label: "Send feedback",
        //    header: "Send us your feedback!",
        //    messageSuccess: "Your issue has been submitted.  Thank you!",
        //    messageError: "Uh oh... something went wrong. Please try again."
        //});

        // Megamenu
        $(".megamenu").megamenu({'show_method':'simple', 'hide_method': 'simple'});

        // Chozen select default
        $(".chzn-select").chosen({ width: '100%', search_contains: true });
        $(".chzn-select-deselect").chosen({ allow_single_deselect:true, width: '100%', search_contains: true });
        $(".chzn-select-deselect").livequery(function(){
            $(this).chosen({allow_single_deselect:true, width:'100%', search_contains: true});
        });


        $(".warehouse-switch").click(function() {
            //$("#warehouse-menu").toggle();
            $("#warehouseMenu").dialog({
                autoOpen: true,
                modal: true,
                width: 800
            });
        });


        function showActions() {
            //$(this).children(".actions").show();
        }

        function hideActions() {
            $(this).children(".actions").hide();
        }

        /* This is used to remove the action menu when the cursor is no longer over the menu */
        $(".action-menu").hoverIntent({
            sensitivity: 1, // number = sensitivity threshold (must be 1 or higher)
            interval: 5,   // number = milliseconds for onMouseOver polling interval
            over: showActions,     // function = onMouseOver callback (required)
            timeout: 100,   // number = milliseconds delay before onMouseOut
            out: hideActions       // function = onMouseOut callback (required)
        });

        // Added to fix bug with the now dynamically load Current Stock tab on the stock card page
        $(".action-menu").livequery(function() {
            $(this).hoverIntent({
                sensitivity: 1, // number = sensitivity threshold (must be 1 or higher)
                interval: 5,   // number = milliseconds for onMouseOver polling interval
                over: showActions,     // function = onMouseOver callback (required)
                timeout: 100,   // number = milliseconds delay before onMouseOut
                out: hideActions       // function = onMouseOut callback (required)
            });
        });


        // Create an action button that toggles the action menu on click
        //button({ text: false, icons: {primary:'ui-icon-gear',secondary:'ui-icon-triangle-1-s'} }).
        /*
         $(".action-btn").click(function(event) {
         $(this).parent().children(".actions").toggle();
         event.preventDefault();
         });
         */
        /*
         $(".action-btn").button({ text: false, icons: {primary:'ui-icon-gear',secondary:'ui-icon-triangle-1-s'} });
         */

        $(".action-btn").live('click', function(event) {
            //show the menu directly over the placeholder
            var actions = $(this).parent().children(".actions");

            // Need to toggle before setting the position
            actions.toggle();

            // Set the position for the actions menu
            actions.position({
                my: "left top",
                at: "left bottom",
                of: $(this).closest(".action-btn"),
                collision: "flip fit"
            });

            // To prevent the action button from POST'ing to the server
            event.preventDefault();
        });

        $(".action-menu-item").click(function(event) {
            var actions = $(this).parent().children(".actions");

            // Need to toggle before setting the position
            actions.toggle();
        });

        $(".action-hover-btn").click(function(event) {
            //show the menu directly over the placeholder
            var actions = $(this).parent().children(".actions");

            // Need to toggle before setting the position
            actions.toggle();

            // Set the position for the actions menu
            actions.position({
                my: "right top",
                at: "right bottom",
                of: $(this).closest(".action-hover-btn"),
                //offset: "0 0"
                collision: "flip"
            });

            // To prevent the action button from POST'ing to the server
            event.preventDefault();
        });

    });
</script>

<g:if test="${session.user && Boolean.valueOf(Holders.config.openboxes.scannerDetection.enabled)}">
    <script src="${createLinkTo(dir:'js/jquery.scannerdetection', file:'jquery.scannerdetection.js')}" type="text/javascript" ></script>
    <script>
        $(document).ready(function() {
            var scanner = $("body").scannerDetection();
            scanner.bind('scannerDetectionComplete',function(event,data){
                console.log("scanner detected");
                console.log(event);
                console.log(data);
                var barcode = data.string;
                $.ajax({
                    dataType: "json",
                    url: "${request.contextPath}/json/scanBarcode?barcode=" + barcode,
                    success: function (data) {
                        console.log(data);
                        if (data.url) {
                            if (confirm("The system has detected that a USB scanner was used and the barcode '" + barcode + "' was successfully found.  You are about to be redirected to the " + data.type + " page (" + data.url + ").\n\nAre you sure you want to redirected?")) {
                                window.location.replace(data.url);
                            }
                        }
                        else {

                            if (confirm("The system has detected that a USB scanner was used, but the barcode '" + barcode + "' was not found.  Would you like to be redirected to Google?")) {
                                window.location.replace("http://www.google.com?q=" + barcode);
                            }
                        }
                    },
                    error: function(xhr, status, error) {
                        console.log(status);
                    }
                });
            });

            scanner.bind('scannerDetectionError',function(event,data){
                //console.log("Error detecting barcode scanner input", event, data);
                //console.log(event);
                //console.log(data);
            });
        });
    </script>
</g:if>
<g:if test="${session.user && Boolean.valueOf(Holders.config.openboxes.uservoice.widget.enabled)}">
    <script type="text/javascript">
        // Include the UserVoice JavaScript SDK (only needed once on a page)
        UserVoice=window.UserVoice||[];(function(){var uv=document.createElement('script');uv.type='text/javascript';uv.async=true;uv.src='//widget.uservoice.com/YkvS1YXcD9o2f8tiOphf5Q.js';var s=document.getElementsByTagName('script')[0];s.parentNode.insertBefore(uv,s)})();

        //
        // UserVoice Javascript SDK developer documentation:
        // https://www.uservoice.com/o/javascript-sdk
        //

        // Set colors
        UserVoice.push(['set', {
            accent_color: '#448dd6',
            trigger_color: 'white',
            trigger_background_color: 'rgba(46, 49, 51, 0.6)'
        }]);

        // Identify the user and pass traits
        // To enable, replace sample data with actual user traits and uncomment the line
        UserVoice.push(['identify', {
            id: '${session?.user?.id}',
            email: '${session?.user?.email}',
            name: '${session?.user?.name}',
            created_at: '${session?.user?.dateCreated?.time}'

            //email:      'john.doe@example.com', // User’s email address
            //name:       'John Doe', // User’s real name
            //created_at: 1364406966, // Unix timestamp for the date the user signed up
            //id:         123, // Optional: Unique id of the user (if set, this should not change)
            //type:       'Owner', // Optional: segment your users by type
            //account: {
            //  id:           123, // Optional: associate multiple users with a single account
            //  name:         'Acme, Co.', // Account name
            //  created_at:   1364406966, // Unix timestamp for the date the account was created
            //  monthly_rate: 9.99, // Decimal; monthly rate of the account
            //  ltv:          1495.00, // Decimal; lifetime value of the account
            //  plan:         'Enhanced' // Plan name for the account
            //}
        }]);

        // Add default trigger to the bottom-right corner of the window:
        UserVoice.push(['addTrigger', {
            mode: 'contact',
            trigger_style: 'tab',
            trigger_position: '${Holders.config.openboxes.uservoice.widget.position?:"bottom-right"}',
            //accent_color: '#448dd6',
            //trigger_color: '#448dd6',
            trigger_background_color: '#448dd6',
            locale: '${session?.user?.locale?:"en"}'
        }]);

        // Or, use your own custom trigger:
        //UserVoice.push(['addTrigger', '#user-voice-trigger', { mode: 'contact' }]);

        // Autoprompt for Satisfaction and SmartVote (only displayed under certain conditions)
        //UserVoice.push(['autoprompt', {}]);
    </script>
</g:if>
<!-- Live Chat -->
<g:if test="${Holders.config.openboxes.zopim.widget.enabled}">
    <!--Start of Zopim Live Chat Script-->
    <script type="text/javascript">
        window.$zopim||(function(d,s){var z=$zopim=function(c){z._.push(c)},$=z.s=
                d.createElement(s),e=d.getElementsByTagName(s)[0];z.set=function(o){z.set.
                _.push(o)};z._=[];z.set._=[];$.async=!0;$.setAttribute("charset","utf-8");
            $.src="${Holders.config.openboxes.zopim.widget.url}";z.t=+new Date;$.
                    type="text/javascript";e.parentNode.insertBefore($,e)})(document,"script");
    </script>
    <!--End of Zopim Live Chat Script-->

    <g:if test="${session.user}">
        <script>
            $zopim(function() {
                $zopim.livechat.setName('${session?.user?.name}');
                $zopim.livechat.setEmail('${session?.user?.email}');
            });
        </script>
    </g:if>
</g:if>
<r:layoutResources/>

</body>
</html>