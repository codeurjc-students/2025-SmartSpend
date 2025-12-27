import { CreateTransactionDto } from "./create-transaction.interface";

export interface CreateTransactionWithImageDto extends CreateTransactionDto {
    imageFile?: File;

}