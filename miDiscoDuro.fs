/*
    Explicación de los números
    1: Es la sección que me indica el tamaño del disco en MB
    2: La cantidad de bloques y el tamaño de cada uno
    3: Lista de los bloques libres(si es uno esta ocupado, si es cero esta libre)
    4: Usuarios en el sistema
    5: Grupos de Usuarios
    6: Los bloques del disco para usar

    Explicación de las letras
    N: Nombre
    U: Usuario
    Con: Contraseña
    GU: grupo usuarios
    B: bloque
    P: Propietarios (Usuario)
    EC: Es carpeta?
    BS: bloque siguiente
    I: información del bloque
    T: Texto del archivo.
*/
[1]
20
[/1]
[2]
10
512
[/2]
[3]
0,1,0,1,1,0,1,0,1,0
[/3]
[4]
[U]
[Id]
0
[/Id]
[N]
root
[/N]
[Con]
root
[/Con]
[/U]
[/4]
[5]
[GU]
[Id]
0
[/Id]
[N]
GrupoRoot
[/N]
[U]
[Id]
0
[/Id]
[/U]
[/GU]
[/5]
[6]
[B]
[Id]
0
[/Id]
[N]
NombreDeLaCarpeta
[/N]
[Ubi]
UbicacionDeLaCarpeta
[/Ubi]
[FCreacion]
FechaDeLaCarpeta
[/FCreacion]
[UModificacion]
UltimaModificacionDeLaCarpeta
[/UModificacion]
[P]
UsuarioPropietarioDeLaCarpeta
[/P]
[/GU]
GrupoUsuariosDeLaCarpeta
[/GU]
[EC]
1
[/EC]
[BS]
-1
[/BS]
[I]
[C]
[N]
CarpetaDentroDeLaCarpeta
[/N]
[BC]
1
[/BC]
[/C]
[A]
[N]
ArchivoDentroDeLaCarpeta
[/N]
[BC]
2
[/BC]
[/A]
[/I]
[/B]
[B]
[Id]
1
[/Id]
[N]
NombreDelArchivo
[/N]
[Ubi]
UbicacionDeLArchivo
[/Ubi]
[FCreacion]
FechaDeLArchivo
[/FCreacion]
[UModificacion]
UltimaModificacionDeLArchivo
[/UModificacion]
[P]
UsuarioPropietarioDeLArchivo
[/P]
[/GU]
GrupoUsuariosDeLArchivo
[/GU]
[EC]
0
[/EC]
[BS]
-1
[/BS]
[I]
[T]
TextoDentroDelArchivo
[/T]
[/I]
[/B]
[B]
[Id]
2
[/Id]
[BS]
-1
[/BS]
[I]
[/I]
[/B]
[/6]