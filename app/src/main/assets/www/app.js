window.map = L.map('map').setView([52.23, 21.01], 13);
L.tileLayer('http://{s}.basemaps.cartocdn.com/light_all/{z}/{x}/{y}.png', {
  attribution: '&copy; <a href="http://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors, &copy; <a href="https://cartodb.com/attributions">CartoDB</a>',
}).addTo(window.map);


window.wifiMarker = L.Marker.movingMarker([[52.23, 21.01]],[],{
    icon: L.ExtraMarkers.icon({
        icon: 'fa-wifi',
        prefix: 'fa',
        markerColor: 'cyan'
    })
}).addTo(map)
window.gpsMarker = L.Marker.movingMarker([[52.235, 21.01]],[],{
     icon: L.ExtraMarkers.icon({
         icon: 'fa-location-arrow',
         prefix: 'fa',
         markerColor: 'green-light'
     })
 }).addTo(map)

window.fitMarkersIntoScreen = function(){
  map.fitBounds(new L.featureGroup([gpsMarker, wifiMarker]).getBounds().pad(0.25));
}

window.updateGpsMarkerLocation = function(lat,lon){
  gpsMarker.moveTo([lat, lon], 500);
  window.fitMarkersIntoScreen();
}
window.geolocateByWifis = function(wifis){
    geoWifis = []
    wifis.forEach(function(item){
        geoWifis.push({
            macAddress: item.bssid,
            signalStrength: item.signal_level
        })
    })
    $.ajax({
        type: "POST",
        url: "https://geowifi.science/api/v1/geolocate",
        data: JSON.stringify({wifiAccessPoints: geoWifis}),
        contentType: "application/json; charset=utf-8",
        dataType: "json",
        success: function(data){
            window.wifiMarker.setLatLng([data.location.lat, data.location.lng]);
            window.fitMarkersIntoScreen();
        },
    });
}
setInterval(window.fitMarkersIntoScreen, 250);