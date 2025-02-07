package tterrag.core;

/**
 * By implementing this interface, your mod package will automatically be added to the handler search and other package
 * based functionality.
 * 
 * It also provides convenience functions for later use.
 */
public interface IModTT {

    String modid();

    String name();

    String version();
}
