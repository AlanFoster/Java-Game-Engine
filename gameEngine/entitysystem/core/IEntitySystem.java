package entitysystem.core;

import java.util.Observer;

import engine.interfaces.ILogical;

/**
 * Defines the interface for an entity system. An entity system is required to
 * be an observer so that it can listen to the EntityManager for refresh list
 * requests (which updates all entity references to the newest in the system)
 * <p>
 * Entity systems also receive logical updates, and it is through the
 * logicUpdate() method that they are expected to perform any entity processing.
 * <p>
 * An entity system controls and manages a list of entity which contain certain
 * components. For instance a 'movement' system will require entities which
 * contain the components such as a Spatial (which stores the location and
 * width/height), and a Movement component, which will store the details of its
 * movement speed etc. The entity system implemented within this game engine are
 * entities and components of the purest form, and by this I mean they contain
 * ONLY the pure raw data, which are generally provided as public fields are
 * fast access. And any logic involved with entities are handled by the entity
 * system. There should be no logic in components what so ever. I believe a
 * possible exception to this may be scripted entities, but this Game Engine
 * does not offer a scripting language for you to do this sadly.
 * 
 * @author Alan Foster
 * @version 1.0
 * 
 */
public interface IEntitySystem extends ILogical, Observer {
	/**
	 * It is through this method that you should consider adding your
	 * "getEntitiesContaining" method calls. This method will be called when an
	 * entity or new component has been added to the EntityManager. Obviously
	 * this will provide some overhead initially, but it is will be less
	 * overhead than calling the same method during every logicUpdate method
	 * call, so it is advised that this logic be placed into this method.
	 * <p>
	 * As it is expected that each entity system will require a different
	 * entityList being populated it is provided as an abstract method, and
	 * should be @Override by any subsystem that requires it
	 */
	public void refreshList();
}
