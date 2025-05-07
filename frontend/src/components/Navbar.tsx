import { Link } from "react-router-dom";

function Navbar() {
  return (
    <nav className="navbar">
      <Link to="/">Home</Link>
      <Link to="/stations">Charging Stations</Link>
    </nav>
  );
}

export default Navbar;
