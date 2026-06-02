// Simplified version - update the relevant part for time input
// Example improvement in placeholder and validation
{unitType === 'TIME' && (
  <input
    type="text"
    placeholder="0 or 00:00"
    onChange={(e) => {
      const val = e.target.value;
      if (val === '' || isValidTimeFormat(val)) {
        // handle score
      }
    }}
  />
)}