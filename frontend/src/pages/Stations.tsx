import { useState, useEffect } from "react";
import axios from "axios";
import Map from "../components/Map";

interface Station {
  id: number;
  name: string;
  location: string;
  status: string;
  availableSlots: number;
  maxSlots: number;
  pricePerkWh: number;
}
function Stations() {
  const [stations, setStations] = useState<Station[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const fetchStations = async () => {
      try {
        const response = await axios.get(
          "http://localhost:8080/api/charging-stations"
        );
        setStations(response.data);
      } catch (error) {
        console.error("Error fetching stations:", error);
      } finally {
        setLoading(false);
      }
    };

    fetchStations();
  }, []);

  if (loading) {
    return <div>Loading stations...</div>;
  }

  return (
    <div>
      <h1>Charging Stations</h1>
      <div style={{ marginBottom: "20px" }}>
        <Map stations={stations} />
      </div>
      {stations.map((station) => (
        <div key={station.id} className="station-card">
          <h3>{station.name}</h3>

          <p>Location: {station.location}</p>
          <p>
            Status:{" "}
            <span className={`status-${station.status.toLowerCase()}`}>
              {station.status}
            </span>
          </p>
          <p>
            Available Slots: {station.availableSlots} / {station.maxSlots}
          </p>
          <p>Price per kWh: ${station.pricePerkWh}</p>
        </div>
      ))}
    </div>
  );
}

export default Stations;
