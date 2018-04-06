package byog.Core;
import org.junit.Assert;
import org.junit.Test;
import static org.junit.Assert.assertEquals;
public class componentsTest {
    @Test
    public void seedGeneratorTest(){
        long result = Game.seedGenerator("ns146s");
        long ref = 146;
        Assert.assertEquals(ref,result);
    }
}
