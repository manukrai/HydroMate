package at.fhj.hydromate;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Beispiel für einen lokalen Unit-Test, der auf der Entwicklungsmaschine (Host) ausgeführt wird.
 *
 * <p>Lokale Unit-Tests laufen unabhängig von Android-Geräten oder Emulatoren. Sie eignen sich für
 * die schnelle Überprüfung von Logik, Berechnungen und Methodenergebnissen ohne Android-spezifischen Kontext.</p>
 *
 * <p>Dieser Test überprüft, ob die Addition korrekt funktioniert.</p>
 *
 * @see <a href="http://d.android.com/tools/testing">Android Testdokumentation</a>
 */
public class ExampleUnitTest {

    /**
     * Testet, ob die Addition von 2 + 2 das erwartete Ergebnis 4 liefert.
     */
    @Test
    public void addition_isCorrect() {
        assertEquals(4, 2 + 2);
    }
}