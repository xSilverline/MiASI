package miasi.backend.enums;

public enum ImpactType {
  QUANTITY_CHANGE,   // np. zniszczenie 100l wody
  EFFICIENCY_CHANGE, // np. spadek wydajności paneli słonecznych o 20%
  STATE_CHANGE       // np. całkowite zepsucie modułu (zmiana statusu na BROKEN)
}