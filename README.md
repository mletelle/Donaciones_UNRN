# Donaciones_UNRN

## Sistema de Gestión de Donaciones

Una organización sin fines de lucro, dedicada a la gestión de donaciones,
quiere implementar un sistema para optimizar el registro y administración
de donaciones. La misma desea contar con un registro de las personas y
entidades dispuestas a realizar donaciones.

## Requerimientos Funcionales

El sistema permitirá el registro de donantes en cualquier momento del año.
Para cada donante, se registrarán sus datos personales, incluyendo nombre,
apellido y DNI, así como su ubicación determinada por dirección, zona,
barrio y coordenadas geográficas (latitud y longitud). Esto permitirá a la
organización gestionar eficientemente las solicitudes de retiro de
donaciones.
Además, la organización contará con un registro de voluntarios encargados
de la recolección y distribución de donaciones. De cada colaborador se
almacenarán datos como nombre, apellido y DNI, facilitando la asignación
de tareas dentro del sistema.
Los donantes podrán notificar a la organización cuando tengan donaciones
disponibles para su retiro. Para ello, el sistema permitirá la creación de
pedidos de donación, los cuales incluirán información como la fecha de
emisión, los bienes a donar (por ejemplo, alimentos, ropa, muebles, etc.),
la necesidad de un vehículo de carga pesada para el traslado y cualquier
observación relevante.
Para gestionar la recolección de donaciones, el sistema generará órdenes de
retiro a partir de los pedidos recibidos. Estas órdenes especificarán qué
personal realizará la recolección, la vivienda o institución a visitar, la fecha y
hora en que se genera la orden y su estado actual, el cual podrá ser
PENDIENTE, EN_EJECUCIÓN o COMPLETADO.
Cada orden de retiro podrá involucrar una o más visitas, dependiendo de la
cantidad de bienes donados y la logística de recolección. En cada visita se
registrará la fecha y hora de realización, la cantidad y tipo de bienes
recogidos, y cualquier observación relevante. Esto permitirá un control
detallado del proceso de donación, del cual se podría tener la siguiente
información:
![alt text](image.png)

El sistema también permitirá el registro de personas o instituciones
beneficiarias que recibirán las donaciones recolectadas. Para cada
beneficiario se almacenarán datos como nombre, tipo (particular o
institución), dirección completa, necesidades específicas, número de
personas a cargo cuando corresponda, y un nivel de prioridad determinado
según la situación socioeconómica. Esta información facilitará la distribución
equitativa y eficiente de los recursos donados.
Para optimizar el control de los bienes recibidos, el sistema gestionará un
inventario centralizado donde se registrarán todas las donaciones
ingresadas al almacén. Cada artículo contará con un código único de
identificación, descripción detallada, estado de conservación, fecha de
ingreso al sistema, fecha de vencimiento cuando sea aplicable, ubicación
específica dentro del almacén, y estado de disponibilidad para su posterior
distribución.
La organización requerirá funcionalidades para la planificación de rutas de
recolección, considerando factores como la ubicación geográfica de los
donantes, la disponibilidad de vehículos y voluntarios, y la capacidad de
carga necesaria. Esta planificación permitirá documentar de manera
detallada el recorrido completo realizado por los equipos recolectores,
identificando cada punto de parada, los volúmenes recolectados, los
tiempos de permanencia y cualquier incidencia relevante, generando así un
registro integral que facilite el seguimiento operativo, la evaluación de
rendimiento y la mejora continua de las operaciones de recolección.
Además se mantendrá un registro completo de vehículos disponibles para
las actividades de recolección y distribución. Para cada vehículo se
almacenará información como matrícula, tipo y modelo, capacidad máxima
de carga, estado de disponibilidad, cronograma de mantenimiento
programado, y asignación a voluntarios que posean la licencia de conducir
correspondiente.
Para completar el ciclo de donaciones, el sistema gestionará la distribución
de bienes a los beneficiarios mediante la generación de órdenes de entrega.
Estas órdenes especificarán el beneficiario destinatario, los productos
asignados según sus necesidades, fecha y hora programada para la entrega,
voluntario responsable de la distribución, y mecanismos para obtener
confirmación de recepción por parte del beneficiario.
El sistema incorporará un módulo de comunicación y notificaciones
automáticas para mantener informados a todos los participantes del
proceso. Se enviarán notificaciones de confirmación de recepción a los
donantes, asignaciones de tareas a los voluntarios, y avisos de
disponibilidad de donaciones a los beneficiarios. La comunicación se
realizará mediante correo electrónico, mensajes SMS o notificaciones push,
respetando las preferencias de contacto de cada usuario.
Adicionalmente, permitirá la organización y gestión de eventos especiales y
campañas de donación masiva, como actividades navideñas, campaña de
donación en empresas, o eventos comunitarios de recolección. Para cada
evento se registrarán datos como fecha de realización, organizadores
responsables, meta de recolección establecida, y resultados finales
obtenidos, permitiendo evaluar el éxito de estas iniciativas especiales.
Finalmente, las donaciones serán clasificadas según su tipo, como alimentos
no perecederos, ropa, mobiliario, productos de higiene, entre otros. Cada
tipo de donación podrá contar con un sistema de categorización o puntaje
que facilite su distribución posterior a quienes más lo necesiten.