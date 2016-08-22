import java.lang.instrument.Instrumentation;
import java.lang.instrument.UnmodifiableClassException;

/**
 * Created by terry on 2016.8.19.
 */
public class Premain {
    public static void premain(String agentArgs, Instrumentation inst) throws ClassNotFoundException, UnmodifiableClassException {
        inst.addTransformer(new Transformer());
//        ClassDefinition def = new ClassDefinition(TransClass.class, Transformer.getBytesFromFile(Transformer.classNumberReturns2));
//        inst.redefineClasses(new ClassDefinition[]{
//                def
//        });
//        System.out.println("success");
    }
}
