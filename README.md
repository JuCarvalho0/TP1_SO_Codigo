# TP1 – Sistemas Operacionais

## Descrição do Projeto

Este projeto foi desenvolvido como parte da disciplina de **Sistemas Operacionais** e tem como objetivo demonstrar conceitos fundamentais relacionados à execução de processos, threads e sincronização em Java.

O trabalho apresenta exemplos práticos de concorrência e comunicação entre tarefas, permitindo compreender como diferentes threads podem compartilhar recursos e executar operações de maneira coordenada.

## Objetivos

- Compreender o funcionamento de threads.
- Estudar conceitos de concorrência.
- Demonstrar o funcionamento do modelo Produtor-Consumidor.
- Aplicar conceitos de sincronização.
- Observar a execução de tarefas concorrentes.

## Tecnologias Utilizadas

- Java
- JDK (Java Development Kit)
- Visual Studio Code
- Terminal PowerShell

## Pré-requisitos

Antes de executar o projeto, é necessário ter:

- Java JDK instalado.
- Variável de ambiente PATH configurada.
- Visual Studio Code ou outro editor de código.

Para verificar a instalação do Java:

```bash
java -version
javac -version
```

## Como executar o projeto

### 1. Abrir a pasta do projeto

Abra o terminal na pasta principal do projeto.

### 2. Compilar os arquivos

No PowerShell, utilize:

```powershell
New-Item -ItemType Directory -Force out
javac -d out (Get-ChildItem -Recurse src -Filter *.java | ForEach-Object FullName)
```

### 3. Executar os exemplos

**Produtor-Consumidor:**

```bash
java -cp out ProdutorConsumidor.ProdCons
```

**Execução da classe Main:**

```bash
java -cp out trabalho.Main 1
```

## Considerações Finais

O desenvolvimento deste trabalho possibilitou colocar em prática conceitos importantes de Sistemas Operacionais, especialmente relacionados à concorrência, threads e sincronização, utilizando a linguagem Java.
