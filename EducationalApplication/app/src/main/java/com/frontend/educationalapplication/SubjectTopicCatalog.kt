package com.frontend.educationalapplication

import android.content.Context

data class LocalStudySubject(
    val area: String,
    val name: String,
    val topics: List<String>
)

object SubjectBases {
    const val HIGH_SCHOOL = "Ensino Médio"
    const val COMPUTING = "Computação"
    const val CUSTOM = "Personalizado"
}

private const val SUBJECT_CATALOG_PREFS = "subject_topic_catalog"
private const val CUSTOM_SUBJECTS_KEY = "custom_subjects"
private const val CUSTOM_TOPIC_PREFIX = "custom_topics_"

private val highSchoolSubjects = listOf(
    LocalStudySubject(SubjectBases.HIGH_SCHOOL, "Matemática", listOf("Função do 1º grau", "Função do 2º grau", "Porcentagem", "Equações do 1º grau", "Equações do 2º grau", "Geometria plana", "Probabilidade", "Estatística", "Juros simples", "Juros compostos")),
    LocalStudySubject(SubjectBases.HIGH_SCHOOL, "Português", listOf("Interpretação de texto", "Gêneros textuais", "Figuras de linguagem", "Pontuação", "Ortografia", "Acentuação", "Classes gramaticais", "Concordância verbal", "Crase", "Sintaxe")),
    LocalStudySubject(SubjectBases.HIGH_SCHOOL, "Redação", listOf("Texto dissertativo-argumentativo", "Tese", "Argumentação", "Repertório sociocultural", "Introdução", "Desenvolvimento", "Conclusão", "Proposta de intervenção", "Coesão", "Competências do ENEM")),
    LocalStudySubject(SubjectBases.HIGH_SCHOOL, "Literatura", listOf("Trovadorismo", "Barroco", "Arcadismo", "Romantismo", "Realismo", "Modernismo", "Semana de Arte Moderna", "Literatura brasileira", "Literatura portuguesa")),
    LocalStudySubject(SubjectBases.HIGH_SCHOOL, "Física", listOf("Cinemática", "Leis de Newton", "Trabalho e energia", "Hidrostática", "Termologia", "Calorimetria", "Óptica", "Ondulatória", "Eletrostática", "Circuitos elétricos")),
    LocalStudySubject(SubjectBases.HIGH_SCHOOL, "Química", listOf("Modelos atômicos", "Tabela periódica", "Ligações químicas", "Funções inorgânicas", "Balanceamento", "Estequiometria", "Soluções", "pH e pOH", "Eletroquímica", "Química orgânica")),
    LocalStudySubject(SubjectBases.HIGH_SCHOOL, "Biologia", listOf("Citologia", "Organelas celulares", "Membrana plasmática", "Genética", "Ecologia", "Evolução", "Botânica", "Zoologia", "Fisiologia humana", "Microbiologia")),
    LocalStudySubject(SubjectBases.HIGH_SCHOOL, "História", listOf("Pré-história", "Antiguidade", "Idade Média", "Renascimento", "Revolução Francesa", "Revolução Industrial", "Brasil Colônia", "Brasil Império", "República", "Guerras Mundiais")),
    LocalStudySubject(SubjectBases.HIGH_SCHOOL, "Geografia", listOf("Cartografia", "Climatologia", "Relevo", "Hidrografia", "População", "Urbanização", "Globalização", "Geopolítica", "Agricultura", "Meio ambiente")),
    LocalStudySubject(SubjectBases.HIGH_SCHOOL, "Filosofia", listOf("Pré-socráticos", "Sócrates", "Platão", "Aristóteles", "Ética", "Política", "Conhecimento", "Iluminismo", "Existencialismo")),
    LocalStudySubject(SubjectBases.HIGH_SCHOOL, "Sociologia", listOf("Cultura", "Sociedade", "Trabalho", "Desigualdade social", "Cidadania", "Movimentos sociais", "Estado", "Globalização", "Indústria cultural")),
    LocalStudySubject(SubjectBases.HIGH_SCHOOL, "Inglês", listOf("Interpretação de texto", "Vocabulário", "Cognatos", "Falsos cognatos", "Simple present", "Simple past", "Present perfect", "Modal verbs", "Passive voice"))
)

private val computingSubjects = listOf(
    LocalStudySubject(SubjectBases.COMPUTING, "Lógica de Programação", listOf("Algoritmos", "Variáveis", "Tipos de dados", "Operadores", "Entrada e saída", "Condicionais", "Laços de repetição", "Funções", "Vetores", "Matrizes", "Recursividade")),
    LocalStudySubject(SubjectBases.COMPUTING, "Programação", listOf("Sintaxe básica", "Controle de fluxo", "Funções e métodos", "Strings", "Arquivos", "Exceções", "Modularização", "Bibliotecas", "Debugging", "Boas práticas")),
    LocalStudySubject(SubjectBases.COMPUTING, "Programação Orientada a Objetos", listOf("Classes", "Objetos", "Atributos", "Métodos", "Construtores", "Encapsulamento", "Herança", "Polimorfismo", "Abstração", "Interfaces")),
    LocalStudySubject(SubjectBases.COMPUTING, "Estruturas de Dados", listOf("Arrays", "Listas", "Pilhas", "Filas", "Árvores", "Árvore binária", "Grafos", "Tabelas hash", "Mapas", "Heap")),
    LocalStudySubject(SubjectBases.COMPUTING, "Algoritmos", listOf("Busca linear", "Busca binária", "Ordenação", "Merge sort", "Quick sort", "Complexidade", "Big O", "Programação dinâmica", "Gulosos", "Backtracking")),
    LocalStudySubject(SubjectBases.COMPUTING, "Banco de Dados", listOf("Modelo entidade-relacionamento", "SQL", "SELECT", "INSERT", "UPDATE", "DELETE", "JOIN", "GROUP BY", "Normalização", "Chave primária", "Chave estrangeira", "Transações", "Índices")),
    LocalStudySubject(SubjectBases.COMPUTING, "Engenharia de Software", listOf("Requisitos", "Casos de uso", "Histórias de usuário", "UML", "Arquitetura", "Padrões de projeto", "MVC", "Testes", "Scrum", "Kanban")),
    LocalStudySubject(SubjectBases.COMPUTING, "Sistemas Operacionais", listOf("Processos", "Threads", "Escalonamento", "Concorrência", "Deadlock", "Memória virtual", "Paginação", "Sistemas de arquivos", "Kernel", "Linux")),
    LocalStudySubject(SubjectBases.COMPUTING, "Redes de Computadores", listOf("Modelo OSI", "TCP/IP", "IP", "DNS", "DHCP", "HTTP", "HTTPS", "TCP", "UDP", "Roteamento", "Wi-Fi", "Serviços web")),
    LocalStudySubject(SubjectBases.COMPUTING, "Desenvolvimento Web", listOf("HTML", "CSS", "JavaScript", "DOM", "Responsividade", "Frontend", "Servidor", "Integrações", "JSON", "React", "Node.js")),
    LocalStudySubject(SubjectBases.COMPUTING, "Desenvolvimento Mobile", listOf("Android", "Activity", "Intent", "Layouts", "Room", "Integração de dados", "Permissões", "Notificações", "Jetpack Compose", "Navegação", "Publicação do app")),
    LocalStudySubject(SubjectBases.COMPUTING, "Segurança da Informação", listOf("Confidencialidade", "Integridade", "Disponibilidade", "Criptografia", "Hash", "Autenticação", "Autorização", "Malware", "Firewall", "OWASP")),
    LocalStudySubject(SubjectBases.COMPUTING, "Arquitetura de Computadores", listOf("CPU", "Memória", "Barramentos", "Registradores", "Cache", "Instruções", "Assembly", "Entrada e saída")),
    LocalStudySubject(SubjectBases.COMPUTING, "Inteligência Artificial", listOf("Busca", "Heurísticas", "Aprendizado de máquina", "Redes neurais", "Processamento de linguagem natural", "Visão computacional", "Ética em IA")),
    LocalStudySubject(SubjectBases.COMPUTING, "Interação Humano-Computador", listOf("Usabilidade", "Acessibilidade", "Prototipação", "Pesquisa com usuários", "Design de interação", "Testes de usabilidade")),
    LocalStudySubject(SubjectBases.COMPUTING, "Matemática para Computação", listOf("Lógica matemática", "Conjuntos", "Relações", "Funções", "Grafos", "Combinatória", "Probabilidade", "Matrizes")),
    LocalStudySubject(SubjectBases.COMPUTING, "DevOps e Ferramentas", listOf("Git", "GitHub", "CI/CD", "Docker", "Linux", "Terminal", "Logs", "Deploy", "Monitoramento"))
)

fun listSubjectsByBase(context: Context, base: String, userId: Long? = null): List<LocalStudySubject> = when (base) {
    SubjectBases.HIGH_SCHOOL -> highSchoolSubjects
    SubjectBases.COMPUTING -> computingSubjects
    SubjectBases.CUSTOM -> mergeSubjects(customSubjects(context) + planningSubjects(context, userId))
    else -> mergeSubjects(highSchoolSubjects + computingSubjects + customSubjects(context) + planningSubjects(context, userId))
}

fun searchSubjects(context: Context, base: String, query: String, userId: Long? = null): List<LocalStudySubject> =
    listSubjectsByBase(context, base, userId).filter { query.isBlank() || it.name.contains(query, ignoreCase = true) }

fun getTopicsForSubject(context: Context, subjectName: String, userId: Long? = null): List<String> {
    val builtIn = (highSchoolSubjects + computingSubjects).firstOrNull { it.name.equals(subjectName, ignoreCase = true) }?.topics.orEmpty()
    val planning = planningSubjects(context, userId).firstOrNull { it.name.equals(subjectName, ignoreCase = true) }?.topics.orEmpty()
    return (builtIn + customTopics(context, subjectName) + planning).distinct()
}

fun searchTopics(context: Context, subjectName: String, query: String, userId: Long? = null): List<String> =
    getTopicsForSubject(context, subjectName, userId).filter { query.isBlank() || it.contains(query, ignoreCase = true) }

fun addCustomSubject(context: Context, name: String) {
    val cleanName = name.trim()
    if (cleanName.isBlank()) return
    val prefs = context.getSharedPreferences(SUBJECT_CATALOG_PREFS, Context.MODE_PRIVATE)
    val updated = prefs.getStringSet(CUSTOM_SUBJECTS_KEY, emptySet()).orEmpty() + cleanName
    prefs.edit().putStringSet(CUSTOM_SUBJECTS_KEY, updated).apply()
}

fun addCustomTopic(context: Context, subjectName: String, topic: String) {
    val cleanSubject = subjectName.trim()
    val cleanTopic = topic.trim()
    if (cleanSubject.isBlank() || cleanTopic.isBlank()) return
    val prefs = context.getSharedPreferences(SUBJECT_CATALOG_PREFS, Context.MODE_PRIVATE)
    val key = CUSTOM_TOPIC_PREFIX + cleanSubject
    val updated = prefs.getStringSet(key, emptySet()).orEmpty() + cleanTopic
    prefs.edit().putStringSet(key, updated).apply()
}

fun addCustomSubjectToPlanning(context: Context, userId: Long, name: String) {
    addCustomSubject(context, name)
    ensureStudyPlanningSubject(context, userId, name)
}

fun addCustomTopicToPlanning(context: Context, userId: Long, subjectName: String, topic: String) {
    addCustomTopic(context, subjectName, topic)
    ensureStudyPlanningTopic(context, userId, subjectName, topic)
}

private fun customSubjects(context: Context): List<LocalStudySubject> =
    context.getSharedPreferences(SUBJECT_CATALOG_PREFS, Context.MODE_PRIVATE)
        .getStringSet(CUSTOM_SUBJECTS_KEY, emptySet())
        .orEmpty()
        .sorted()
        .map { LocalStudySubject(SubjectBases.CUSTOM, it, customTopics(context, it)) }

private fun customTopics(context: Context, subjectName: String): List<String> =
    context.getSharedPreferences(SUBJECT_CATALOG_PREFS, Context.MODE_PRIVATE)
        .getStringSet(CUSTOM_TOPIC_PREFIX + subjectName, emptySet())
        .orEmpty()
        .sorted()

private fun planningSubjects(context: Context, userId: Long?): List<LocalStudySubject> {
    val safeUserId = userId ?: return emptyList()
    return loadStudyPlanningSubjects(context, safeUserId).map { subject ->
        LocalStudySubject(
            area = SubjectBases.CUSTOM,
            name = subject.name,
            topics = subject.topics.map { it.name }
        )
    }
}

private fun mergeSubjects(subjects: List<LocalStudySubject>): List<LocalStudySubject> =
    subjects
        .groupBy { it.name.lowercase() }
        .values
        .map { group ->
            val first = group.first()
            first.copy(topics = group.flatMap { it.topics }.distinct())
        }
        .sortedBy { it.name.lowercase() }
