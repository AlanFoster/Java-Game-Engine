package entitysystem.core;

/**
 * Any object that wishes to be attached to a entity should implement this
 * interface so that it can be accepted by the entity manager
 * <p>
 * IComponent does not extend ICloneable as templates should be provided in such
 * scenarios in which you want to copy many component's details.
 * 
 * @see Entity
 * @see EntityManager
 * @see EntityTemplate
 */
public interface IComponent {
}