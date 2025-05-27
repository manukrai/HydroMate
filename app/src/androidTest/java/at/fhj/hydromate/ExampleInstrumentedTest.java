package at.fhj.hydromate;

import android.content.Context;

import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.*;

/**
 * Instrumentierter Test, der auf einem Android-Gerät oder Emulator ausgeführt wird.
 *
 * <p>Diese Klasse enthält einen Beispiel-Test, der sicherstellt, dass der Anwendungskontext
 * korrekt geladen wird und zum erwarteten Paketnamen gehört.</p>
 *
 * <p>Instrumentierte Tests werden mit dem AndroidJUnitRunner ausgeführt und eignen sich für
 * UI-Tests, Interaktionen mit dem Systemkontext oder Abfragen von Android-spezifischen Ressourcen.</p>
 *
 * @see <a href="http://d.android.com/tools/testing">Android Testdokumentation</a>
 */
@RunWith(AndroidJUnit4.class)
public class ExampleInstrumentedTest {

    /**
     * Testet, ob der Anwendungskontext korrekt initialisiert ist und den richtigen Paketnamen hat.
     */
    @Test
    public void useAppContext() {
        // Context of the app under test.
        Context appContext = InstrumentationRegistry.getInstrumentation().getTargetContext();
        assertEquals("at.fhj.hydromate", appContext.getPackageName());
    }
}