/**
 * 
 */
function initmap(latitude, longitude, name, idname) {
	var latlng = new google.maps.LatLng(latitude, longitude);

	var mapOptions = {
		center: latlng,
		zoom: 16,
		mapTypeId: google.maps.MapTypeId.ROADMAP,
		streetViewControl: false
	};
	var map = new google.maps.Map(document.getElementById(idname), mapOptions);

	var marker = new google.maps.Marker({
		position: latlng,
		map: map,
		animation: google.maps.Animation.DROP,
		title: name,
		draggable: false
	});

	google.maps.event.addDomListener(marker, 'dragend', function() { markerMoved(marker, name); });

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
	return marker;
}

function markerMoved(marker, name) {
	$.ajax( {
		type:'Post',
		url:'https://glass-remindme.appspot.com/main',
		data: {
			operation: "updateMarker",
			tag: name,
			latitude: marker.position.lat(),
			longitude: marker.position.lng()
			},
		success:function(data) {
//		 alert(data);
		}

		})
}

function markerEditable(marker, flag) {
	var draggable=marker.getDraggable();
	console.log("draggable="+draggable)
	marker.setDraggable(!draggable);
}
