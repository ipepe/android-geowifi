window.map = L.map('map').setView([52.23, 21.01], 13);
L.tileLayer('http://{s}.basemaps.cartocdn.com/light_all/{z}/{x}/{y}.png', {
  attribution: '&copy; <a href="http://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors, &copy; <a href="https://cartodb.com/attributions">CartoDB</a>',
}).addTo(window.map);

function distance(lat1, lon1, lat2, lon2) {
  var p = 0.017453292519943295;    // Math.PI / 180
  var c = Math.cos;
  var a = 0.5 - c((lat2 - lat1) * p)/2 +
          c(lat1 * p) * c(lat2 * p) *
          (1 - c((lon2 - lon1) * p))/2;

  return 12742000 * Math.asin(Math.sqrt(a)); // 2 * R; R = 6371 km
}

window.googleTimes = 0;
window.googleAccuracy = 0;
window.googleMeasuredAccuracy = 0;

window.geowifiTimes = 0;
window.geowifiAccuracy = 0;
window.geowifiMeasuredAccuracy = 0;

window.wifiMarker = L.Marker.movingMarker([[52.23, 21.01]],[],{
    icon: L.ExtraMarkers.icon({
        icon: 'fa-wifi',
        prefix: 'fa',
        markerColor: 'cyan'
    })
}).addTo(map)
window.googleWifiMarker = L.Marker.movingMarker([[52.23, 21.02]],[],{
    icon: L.ExtraMarkers.icon({
        icon: 'fa-wifi',
        prefix: 'fa',
        markerColor: 'orange'
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
  map.fitBounds(new L.featureGroup([gpsMarker, wifiMarker, googleWifiMarker]).getBounds().pad(0.25));
//  map.fitBounds(new L.featureGroup([gpsMarker, wifiMarker]).getBounds().pad(0.25));
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
            if(data){
                geowifiTimes += 1;
                window.geowifiAccuracy += parseFloat(data.accuracy || 0)
                window.geowifiMeasuredAccuracy += distance(gpsMarker.getLatLng().lat, gpsMarker.getLatLng().lng, data.location.lat, data.location.lng)
                window.wifiMarker.setLatLng([data.location.lat, data.location.lng]);
                window.fitMarkersIntoScreen();
            }
        },
    });
    $.ajax({
        type: "POST",
        url: ,
        data: JSON.stringify({wifiAccessPoints: geoWifis}),
        contentType: "application/json; charset=utf-8",
        dataType: "json",
        success: function(data){
            if(data){
                googleTimes += 1;
                window.googleMeasuredAccuracy += distance(gpsMarker.getLatLng().lat, gpsMarker.getLatLng().lng, data.location.lat, data.location.lng)
                window.googleAccuracy += parseFloat(data.accuracy || 0)
                window.googleWifiMarker.setLatLng([data.location.lat, data.location.lng]);
                window.fitMarkersIntoScreen();
            }
        },
    });
}
setInterval(window.fitMarkersIntoScreen, 250);

var timeStart = new Date();
var info = L.control();
info.onAdd = function (map) {
    this._div = L.DomUtil.create('div', 'info'); // create a div with a class "info"
    return this._div;
};
info.update = function (props) {
    this._div.innerHTML = '<strong>Średni czas:</strong>';
    this._div.innerHTML += '<br>GeoWifi: ' + (  (((new Date()) - timeStart)/1000) / geowifiTimes ).toFixed(3) + ' s';
    this._div.innerHTML += '<br>Google: ' + (  (((new Date()) - timeStart)/1000) / googleTimes ).toFixed(3) + ' s';

    this._div.innerHTML += '<br><strong>Średnia deklarowana precyzja:</strong>';
    this._div.innerHTML += '<br>GeoWifi: ' + parseInt(  geowifiAccuracy / geowifiTimes ) + ' m';
    this._div.innerHTML += '<br>Google: ' + parseInt(  googleAccuracy / googleTimes ) + ' m';

    this._div.innerHTML += '<br><strong>Średnia zmierzona precyzja:</strong>';
    this._div.innerHTML += '<br>GeoWifi: ' + parseInt(  geowifiMeasuredAccuracy / geowifiTimes ) + ' m';
    this._div.innerHTML += '<br>Google: ' + parseInt(  googleMeasuredAccuracy / googleTimes ) + ' m';

    this._div.innerHTML += '<br><strong>Liczba geolokacji:</strong>';
    this._div.innerHTML += '<br>GeoWifi: ' + parseInt( geowifiTimes );
    this._div.innerHTML += '<br>Google: ' + parseInt(  googleTimes );



};
info.addTo(window.map);

setInterval(function(){
    info.update();
}, 1000)


window.test = function(){
    window.geolocateByWifis([{bssid: '88Ad43f1af4a', signal_level: -75}, {bssid: 'fc94e32ff089', signal_level: -50}, {bssid: '00AA70D94A3C', signal_level: -75}])
}