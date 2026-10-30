import java.util.ArrayList;
import java.util.Scanner;

public class Cotizador {

    //  PATRON 1: STRATEGY (Comportamiento)
    //  Descuento según el tipo de cliente
    interface EstrategiaDescuento {
        double aplicar(double subtotal);
        String nombre();
    }
    // Estrategia para un descuento del 5% para clientes particulares
    static class DescuentoParticular implements EstrategiaDescuento {
        public double aplicar(double subtotal) { return subtotal * 0.95; }
        public String nombre() { return "5% cliente particular"; }
    }
    // Estrategia para un descuento del 10% para clientes empresa
    static class DescuentoEmpresa implements EstrategiaDescuento {
        public double aplicar(double subtotal) { return subtotal * 0.90; }
        public String nombre() { return "10% cliente empresa"; }
    }

 
    //  PATRON 2: DECORATOR (Estructural)
    //  Descuentos combinables (por producto o sobre el total),  se pueden juntar y crear nuevos descuentos.

    // Interfaz común para la cotización
    interface Cotizacion {
        ArrayList<String> getNombres();
        ArrayList<Double> getPrecios();
        double total();
        String describir();
    }

    // Comportamiento comun en cualquier cotización (sin descuentos)
    static class CotizacionBase implements Cotizacion {
        private final ArrayList<String> nombres;
        private final ArrayList<Double> precios;

        public CotizacionBase(ArrayList<String> nombres, ArrayList<Double> precios) {
            this.nombres = nombres;
            this.precios = precios;
        }

        public ArrayList<String> getNombres() { return nombres; }
        public ArrayList<Double> getPrecios() { return precios; }

        public double total() {
            double suma = 0;
            for (double p : precios) suma += p;
            return suma;
        }

        public String describir() { return "Subtotal: Bs" + total(); }
    }

    //Decorador padre que contiene a la cotizacion  y sus metodos.

    static abstract class DecoradorDescuento implements Cotizacion {
        protected final Cotizacion cotizacion;
        protected DecoradorDescuento(Cotizacion cotizacion) { this.cotizacion = cotizacion; }

        public ArrayList<String> getNombres() { return cotizacion.getNombres(); }
        public ArrayList<Double> getPrecios() { return cotizacion.getPrecios(); }
    }

    //Primer decorador que aplica un descuento a un producto especifico.

    static class DescuentoProducto extends DecoradorDescuento {
        private final String producto;
        private final double porcentaje;

        public DescuentoProducto(Cotizacion cotizacion, String producto, double porcentaje) {
            super(cotizacion);
            this.producto = producto;
            this.porcentaje = porcentaje;
        }

        public double total() {
            double base = cotizacion.total();
            for (int i = 0; i < getNombres().size(); i++) {
                if (getNombres().get(i).equalsIgnoreCase(producto)) {
                    base -= getPrecios().get(i) * porcentaje;
                    break;
                }
            }
            return base;
        }

        public String describir() {
            return cotizacion.describir() + "\n" + (porcentaje * 100) + "% en " + producto;
        }
    }

    //Segundo decorador que aplica un descuento sobre el total de la cotizacion.

    static class DescuentoTotal extends DecoradorDescuento {
        private final double porcentaje;

        public DescuentoTotal(Cotizacion cotizacion, double porcentaje) {
            super(cotizacion);
            this.porcentaje = porcentaje;
        }

        public double total() { return cotizacion.total() * (1 - porcentaje); }

        public String describir() {
            return cotizacion.describir() + "\n-" + (porcentaje * 100) + "% sobre total";
        }
    }

    //  Contexto: une la cotización con la estrategia
    static class CotizacionConEstrategia {
        private final Cotizacion cotizacion;
        private final EstrategiaDescuento estrategia;

        public CotizacionConEstrategia(Cotizacion c, EstrategiaDescuento e) {
            this.cotizacion = c;
            this.estrategia = e;
        }

        public double totalFinal() { return estrategia.aplicar(cotizacion.total()); }
        public Cotizacion getCotizacion() { return cotizacion; }
        public EstrategiaDescuento getEstrategia() { return estrategia; }
    }


    //  PATRÓN 3: FACTORY METHOD (Creacional)
    static abstract class CreadorCotizacion {
        protected abstract EstrategiaDescuento crearEstrategia(); 

        public CotizacionConEstrategia crear(Cotizacion c) {
            return new CotizacionConEstrategia(c, crearEstrategia());
        }
    }

    static class CreadorParticular extends CreadorCotizacion {
        protected EstrategiaDescuento crearEstrategia() { return new DescuentoParticular(); }
    }

    static class CreadorEmpresa extends CreadorCotizacion {
        protected EstrategiaDescuento crearEstrategia() { return new DescuentoEmpresa(); }
    }

    //  PROGRAMA PRINCIPAL
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);

        System.out.println("COTIZADOR DE COMPONENTES PC");
        System.out.print("Tipo de cliente (particular/empresa): ");
        String tipo = sc.nextLine().trim();

        // Crea un tipo de cotización según el tipo de cliente (Factory Method)        
        CreadorCotizacion creador;
        if (tipo.equalsIgnoreCase("empresa")) creador = new CreadorEmpresa();
        else creador = new CreadorParticular();

        ArrayList<String> nombres = new ArrayList<>();
        ArrayList<Double> precios = new ArrayList<>();
        // Pide los datos
        System.out.print("Cantidad de productos: ");
        int n = Integer.parseInt(sc.nextLine().trim());
        for (int i = 0; i < n; i++) {
            System.out.print("Producto " + (i + 1) + " (nombre): ");
            nombres.add(sc.nextLine().trim());
            System.out.print("Precio: ");
            precios.add(Double.parseDouble(sc.nextLine().trim()));
        }

        // Decoradores opcionales
        Cotizacion c = new CotizacionBase(nombres, precios);
        
        //Se introduce si se desea aplicar un descuento extra a un producto especifico.
        //Se reescribe el objeto c con el decorador DescuentoProducto si se desea aplicar el descuento.
        System.out.print("Aplicar 5% extra a algun producto? (nombre o vacio): ");
        String prod = sc.nextLine().trim();
        if (!prod.isEmpty()) c = new DescuentoProducto(c, prod, 0.05);
        //Se introduce si se desea aplicar un descuento al total de la cotizacion.
        //Se reescribe el objeto c con el decorador DescuentoTotal si se desea aplicar el descuento.
        System.out.print("Aplicar 10% extra sobre el total? (s/n): ");
        if (sc.nextLine().trim().equalsIgnoreCase("s")) c = new DescuentoTotal(c, 0.10);

        // Factory Method crea el contexto con la estrategia correcta
        CotizacionConEstrategia cot = creador.crear(c);

        // Se devuelve el detalle final de la cotizacion 
        System.out.println("\nDETALLE");
        for (int i = 0; i < nombres.size(); i++)
            System.out.printf(nombres.get(i) + precios.get(i));
        System.out.println(cot.getCotizacion().describir());
        System.out.printf("Estrategia cliente: " + cot.getEstrategia().nombre() + "%n");
        System.out.printf("TOTAL FINAL:" + cot.totalFinal() + " %n");
        sc.close();
    }
}