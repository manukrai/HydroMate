package at.fhj.hydromate.helper;

/**
 * Das {@code LocationCallback}-Interface definiert eine Rückrufmethode zur Übergabe
 * von Standortdaten (Koordinaten), die durch eine Standortabfrage ermittelt wurden.
 *
 * <p>
 * Implementiere dieses Interface, um auf Standortdaten zu reagieren, sobald sie verfügbar sind.
 * Die Methode {@link #onLocationReceived(String)} wird aufgerufen, wenn der aktuelle Standort
 * erfolgreich ermittelt wurde oder ein Fehler aufgetreten ist.
 * </p>
 *
 * <p>
 * Das Format des {@code coordinates}-Parameters entspricht in der Regel: {@code "Breitengrad,Längengrad"},
 * z. B. {@code "47.0707,15.4395"}.
 * Im Fehlerfall kann der Parameter eine Fehlerbeschreibung enthalten, z. B. {@code "Location not available"}.
 * </p>
 *
 * Beispielimplementierung:
 * <pre>{@code
 * LocationCallback callback = new LocationCallback() {
 *     @Override
 *     public void onLocationReceived(String coordinates) {
 *         Log.d("Standort", "Empfangene Koordinaten: " + coordinates);
 *     }
 * };
 * }</pre>
 */
public interface LocationCallback {

    /**
     * Wird aufgerufen, wenn der Standort ermittelt wurde oder ein Fehler aufgetreten ist.
     *
     * @param coordinates Koordinaten im Format "Breitengrad,Längengrad" oder eine Fehlermeldung.
     */
    void onLocationReceived(String coordinates);
}

