/** formato unico de erro devolvido pela API (400, 401, 403, 404, 409, 500). */
export interface ErroApi {
  timestamp: string;
  status: number;
  erro: string;
  mensagem: string;
  caminho: string;
  campos: { campo: string; mensagem: string }[];
}
