import React from 'react';
import { GoogleMap, LoadScript, Marker } from '@react-google-maps/api';

const containerStyle = {
  width: '100%',
  height: '400px'
};

const defaultCenter = {
  lat: 41.3851,  // Default to Lisbon coordinates
  lng: 2.1734
};

function Map({ stations }) {
  return (
    <LoadScript googleMapsApiKey="YOUR_GOOGLE_MAPS_API_KEY">
      <GoogleMap
        mapContainerStyle={containerStyle}
        center={defaultCenter}
        zoom={12}
      >
        {stations.map(station => (
          <Marker
            key={station.id}
            position={{
              lat: station.latitude,
              lng: station.longitude
            }}
            title={station.name}
          />
        ))}
      </GoogleMap>
    </LoadScript>
  );
}

export default Map; 