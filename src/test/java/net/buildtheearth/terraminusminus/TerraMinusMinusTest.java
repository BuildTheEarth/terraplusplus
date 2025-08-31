package net.buildtheearth.terraminusminus;

import org.apache.logging.log4j.core.config.Configurator;
import org.junit.jupiter.api.BeforeEach;

import static org.apache.logging.log4j.Level.*;

public abstract class TerraMinusMinusTest {

    @BeforeEach
    public void setup() {
        Configurator.setRootLevel(DEBUG);
    }

}
