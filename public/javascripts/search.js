angular
		.module('search', [])
		.service("searchService", function($rootScope, $http) {
			return {
				self: this,
				updating : false,
				search : function(searchurl, query, page) {					
					if (!self.updating) {
						self.updating = true;
						$http.get(searchurl, {
							params : {
								terms : query,
								page : page
							}
						}).then(function(data) {
							$rootScope.$broadcast("searchComplete", data.data);
							self.updating = false;
							
						});
					}
				}
			}
		})
		.directive(
				'lrsearch',
				function() {
					return {
						restrict : "E",
						transclude : true,
						scope : {
							searchUrl : "=searchUrl"
						},
						link : function($scope, $element, $attrs) {
							$scope.searchurl = $attrs.searchurl
						},
						controller : function($scope, searchService) {
							$(window)
									.bind(
											"scroll",
											function() {
												var nVScroll = document.documentElement.scrollTop
														|| document.body.scrollTop;
												var scrollHeight = document.documentElement.scrollHeight
														|| document.body.scrollHeight;
												if (((nVScroll / scrollHeight) * 100) >= 66.0) {
													updateResults();
												}
											});
							$scope.query = "";
							$scope.page = 0;
							var updateResults = function() {
								searchService.search($scope.searchurl,
										$scope.query, $scope.page);
							}
							$scope.$on('searchComplete', function(e, args) {
								$scope.page++;
							});
							$scope.search = function() {
								$scope.page = 0;
								updateResults();
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
							$scope.results = [], $scope
									.$on('searchComplete',
											function(e, args) {
												angular.copy(args.data,
														$scope.results);
											})
						},
						template : '<dl ng-repeat="result in results">'
								+ '<dt><a href="{{result.url}}">{{result.title}}</a></dt>'
								+ '<dd>'
								+ '<p class="text-muted">{{result.url}}</p>'
								+ '<p>{{result.description}}</p>' + '</dd>'
								+ '</dl>'
					}
				});