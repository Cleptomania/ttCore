package tterrag.core.common.imc;

import java.util.List;

import com.google.common.collect.Lists;

import cpw.mods.fml.common.event.FMLInterModComms.IMCEvent;
import cpw.mods.fml.common.event.FMLInterModComms.IMCMessage;
import tterrag.core.common.imc.handlers.IMCRightClickCrop;

public class IMCRegistry {

    public interface IIMC {

        String getKey();

        void act(IMCMessage msg);
    }

    public static abstract class IMCBase implements IIMC {

        private String key;

        public IMCBase(String key) {
            this.key = key;
        }

        @Override
        public String getKey() {
            return key;
        }
    }

    public static final IMCRegistry INSTANCE = new IMCRegistry();

    private List<IIMC> handlers = Lists.newArrayList();

    private IMCRegistry() {}

    public void addIMCHandler(IIMC handler) {
        handlers.add(handler);
    }

    public void handleEvent(IMCEvent event) {
        for (IIMC handler : handlers) {
            for (IMCMessage msg : event.getMessages()) {
                if (msg.key.equals(handler.getKey())) {
                    handler.act(msg);
                }
            }
        }
    }

    public void init() {
        addIMCHandler(new IMCRightClickCrop());
    }
}
