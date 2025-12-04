package utec.proyectofinal.Proyecto.Final.UTEC.business.repositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import utec.proyectofinal.Proyecto.Final.UTEC.business.entities.Usuario;
import utec.proyectofinal.Proyecto.Final.UTEC.enums.EstadoUsuario;
import utec.proyectofinal.Proyecto.Final.UTEC.enums.Rol;

import java.util.List;
import java.util.Optional;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Integer> {
    
    Optional<Usuario> findByNombre(String nombre);
    Optional<Usuario> findByNombreIgnoreCase(String nombre);
    
    Optional<Usuario> findByEmail(String email);
    Optional<Usuario> findByEmailIgnoreCase(String email);
    
    List<Usuario> findByEstado(EstadoUsuario estado);
    Page<Usuario> findByEstado(EstadoUsuario estado, Pageable pageable);
    
    List<Usuario> findAllByRol(Rol rol);
    Page<Usuario> findByRol(Rol rol, Pageable pageable);
    Page<Usuario> findByActivo(Boolean activo, Pageable pageable);
    Optional<Usuario> findByRol(Rol rol);
        
    boolean existsByNombreIgnoreCase(String nombre);
    boolean existsByEmailIgnoreCase(String email);
    boolean existsByRol(Rol rol);
    
    
    @Query("SELECT u FROM Usuario u WHERE u.estado = :estado AND " +
           "(LOWER(u.nombre) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(u.nombres) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(u.apellidos) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(u.email) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<Usuario> findByEstadoAndSearchTerm(@Param("estado") EstadoUsuario estado, 
                                             @Param("search") String search, 
                                             Pageable pageable);
    
    @Query("SELECT u FROM Usuario u WHERE " +
           "LOWER(u.nombre) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(u.nombres) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(u.apellidos) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(u.email) LIKE LOWER(CONCAT('%', :search, '%'))")
    Page<Usuario> findBySearchTerm(@Param("search") String search, Pageable pageable);
    
    
    @Query("SELECT u FROM Usuario u WHERE u.rol = :rol AND " +
           "(LOWER(u.nombre) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(u.nombres) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(u.apellidos) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(u.email) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<Usuario> findBySearchTermAndRol(@Param("search") String search, 
                                          @Param("rol") Rol rol, 
                                          Pageable pageable);
    
    
    @Query("SELECT u FROM Usuario u WHERE u.activo = :activo AND " +
           "(LOWER(u.nombre) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(u.nombres) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(u.apellidos) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(u.email) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<Usuario> findByActivoAndSearchTerm(@Param("activo") Boolean activo,
                                             @Param("search") String search, 
                                             Pageable pageable);
    
    
    @Query("SELECT u FROM Usuario u WHERE u.rol = :rol AND u.activo = :activo AND " +
           "(LOWER(u.nombre) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(u.nombres) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(u.apellidos) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(u.email) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<Usuario> findByRolAndActivoAndSearchTerm(@Param("rol") Rol rol,
                                                    @Param("activo") Boolean activo,
                                                    @Param("search") String search, 
                                                    Pageable pageable);
    
    
    Page<Usuario> findByRolAndActivo(Rol rol, Boolean activo, Pageable pageable);
}
