/**
 * 
 */
function initmap(latitude, longitude, name, idname) {
	var latlng = new google.maps.LatLng(latitude, longitude);

	var mapOptions = {
		center: latlng,
		zoom: 14,
		mapTypeId: google.maps.MapTypeId.ROADMAP,
		streetViewControl: false
	};
	var map = new google.maps.Map(document.getElementById(idname), mapOptions);

	var marker = new google.maps.Marker({
		position: latlng,
		title: name
	});
	marker.setMap(map);

	var circleOptions = {
		strokeColor: "#FF0000",
		strokeOpacity: 0.8,
		strokeWeight: 2,
		fillColor: "#FF0000",
		fillOpacity: 0.35,
		map: map,
		center: latlng,
		radius: 100
	};
	circle = new google.maps.Circle(circleOptions);
}
