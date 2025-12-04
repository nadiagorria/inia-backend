package utec.proyectofinal.Proyecto.Final.UTEC.business.repositories.projections;


public interface AnalisisPorAprobarProjection {
    Long getAnalisisID();
    String getTipoAnalisis(); // String porque viene de la query SQL
    Long getLoteID();
    String getNomLote();
    String getFicha();
    String getEspecieNombre();
    String getCultivarNombre();
    String getFechaInicio();
    String getFechaFin();
}
