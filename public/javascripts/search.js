angular
		.module('lr.search', [])
		.service(
				"searchService",
				function($rootScope, $http) {
					return {
						query : "",
						page : 0,
						searchUrl : "",
						searchResults : [],
						broadcastResults : function(data) {
							this.searchResults = this.searchResults.concat(data);
							$rootScope.$broadcast("searchComplete",this.searchResults);
						},
						loadPage : function() {
							var self = this;
							$http.get(this.searchUrl + "search", {
								params : {
									terms : this.query,
									page : this.page
								}
							}).then(function(data){
								var data = data.data.data.map(function(item){
									item.screenShotUrl = self.searchUrl + "screenshot/" + item._id;
									return item;
								});								
								self.broadcastResults(data);
							});
							this.page++;
						},
						search : function() {
							this.searchResults = [];
							this.page = 0;
							this.loadPage();
						},
						loadNextPage: function() {
							this.loadPage();
						},
						similiar : function(docId) {
							$http.get(this.searchUrl + 'similiar/' + docId, {
								params : {
									page : this.page
								}
							}).then(this.broadcastResults);
							this.page++;
						}

					}
				})
		.directive(
				'lrsearch',
				function() {
					return {
						restrict : "E",
						transclude : true,
						scope : {},
						link : function($scope, $element, $attrs, searchService) {
							var searchurl = $attrs.endpoint;
							$scope.searchUrl = searchurl;
						},
						controller : function($scope, searchService) {
							$scope.search = function() {
								searchService.searchUrl = $scope.searchUrl;
								searchService.query = $scope.query;
								searchService.search();
							}

						},
						template : '<form ng-submit="search()">'
								+ '<input placeholder="Search" id="q" ng-model="query""></input>'
								+ '<button id="search" class="btn btn-primary" type="submit">Search</button>'
								+ '</form>'
					}
				})
		.directive(
				'lrresults',
				function() {
					return {
						restrict : "E",
						transclude : true,
						scope : {},
						controller : function($scope, searchService) {
							$scope.results = [];
							$scope.$on('searchComplete', function(e, args) {
								angular.copy(args, $scope.results);
							});
							$scope.next = function() {
								searchService.loadNextPage();
							}
						},
						template : '<dl ng-repeat="result in results">'
								+ '<dt><a href="{{result.url}}">{{result.title}}</a></dt>'
								+ '<dd>'
								+ '<a href="{{result.url}}"><img src="{{result.screenShotUrl}}"/></a><br/>'
								+ '<p class="text-muted">{{result.url}}</p>'
								+ '<p>{{result.description}}</p>'
								+ '<a href="#/review/{{result._id}}" class="text-muted">Review</a>'
								+ '</dd>'
								+ '</dl>'
								+ '<button ng-click="next()" class="btn">Next</button>'
					}
				});