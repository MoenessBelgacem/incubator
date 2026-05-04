package com.enicarthage.incubator.config;

import com.enicarthage.incubator.model.*;
import com.enicarthage.incubator.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final SessionRepository sessionRepository;
    private final RoundRepository roundRepository;
    private final SessionQuestionRepository questionRepository;
    private final ApplicationRepository applicationRepository;
    private final ProjectRepository projectRepository;
    private final QuestionnaireAnswerRepository answerRepository;
    private final EvaluationRepository evaluationRepository;
    private final RoundSelectionOverrideRepository overrideRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) {
        if (sessionRepository.count() > 0) {
            log.info("ℹ️ Données déjà présentes. Nettoyage...");
            overrideRepository.deleteAll();
            answerRepository.deleteAll();
            evaluationRepository.deleteAll();
            projectRepository.deleteAll();
            applicationRepository.deleteAll();
            questionRepository.deleteAll();
            roundRepository.deleteAll();
            sessionRepository.deleteAll();
        }

        log.info("🌱 Initialisation du jeu de données complet...");

        // ══════════════════════════════════════════════════════════════
        // 1. USERS
        // ══════════════════════════════════════════════════════════════
        User admin = mkUser("Admin", "Enicarthage", "admin@enicarthage.tn", Role.ADMIN, null);
        User evalTech = mkUser("Sami", "Gharbi", "sami.tech@enicarthage.tn", Role.EVALUATOR, null);
        User evalBiz  = mkUser("Amira", "Trabelsi", "amira.biz@enicarthage.tn", Role.EVALUATOR, null);
        User evalGen  = mkUser("Karim", "Benali", "karim.gen@enicarthage.tn", Role.EVALUATOR, null);

        User s1 = mkUser("Youssef", "Karray",  "youssef@enicarthage.tn", Role.STUDENT, "Génie Logiciel");
        User s2 = mkUser("Fatma",   "Zahra",   "fatma@enicarthage.tn",   Role.STUDENT, "Génie Électrique");
        User s3 = mkUser("Ahmed",   "Mansour", "ahmed@enicarthage.tn",   Role.STUDENT, "Génie Industriel");
        User s4 = mkUser("Nour",    "Bouzid",  "nour@enicarthage.tn",    Role.STUDENT, "Télécoms");
        User s5 = mkUser("Omar",    "Jebali",  "omar@enicarthage.tn",    Role.STUDENT, "Génie Civil");
        User s6 = mkUser("Ines",    "Gassoumi","ines@enicarthage.tn",    Role.STUDENT, "Mécatronique");
        User s7 = mkUser("Ali",     "Trabelsi","ali@enicarthage.tn",     Role.STUDENT, "Génie Logiciel");
        User s8 = mkUser("Mariem",  "Selmi",   "mariem@enicarthage.tn",  Role.STUDENT, "Réseaux");

        Set<User> allEvals  = Set.of(evalTech, evalBiz, evalGen);
        Set<User> techEvals = Set.of(evalTech, evalGen);

        // ══════════════════════════════════════════════════════════════
        // 2. SESSION CLOSED — fully completed, all rounds done
        //    Tests: history, COMPLETED status, mentoring button
        // ══════════════════════════════════════════════════════════════
        Session sessClosed = sessionRepository.save(Session.builder()
                .name("Bootcamp IA & Big Data 2024")
                .description("Programme intensif pour les solutions IA.")
                .startDate(LocalDate.now().minusYears(1))
                .endDate(LocalDate.now().minusMonths(6))
                .status(SessionStatus.CLOSED).build());

        Round cR1 = roundRepository.save(Round.builder().session(sessClosed).name("Sélection sur Dossier")
                .orderIndex(1).roundNumber(1).status(RoundStatus.COMPLETED)
                .evaluators(allEvals).passingCandidatesCount(2)
                .juryPresident(evalTech).selectionValidated(true).selectionFinalized(true)
                .deadline(LocalDate.now().minusMonths(10)).build());
        Round cR2 = roundRepository.save(Round.builder().session(sessClosed).name("Pitch Final")
                .orderIndex(2).roundNumber(2).status(RoundStatus.COMPLETED)
                .evaluators(allEvals).passingCandidatesCount(1)
                .juryPresident(evalBiz).selectionValidated(true).selectionFinalized(true)
                .deadline(LocalDate.now().minusMonths(7)).build());

        addQs(cR1, Arrays.asList(
                q("Nom de la solution IA", QuestionType.TEXT, true, 0, null),
                q("Cas d'usage principal", QuestionType.TEXTAREA, true, 1, null)));
        addQs(cR2, Arrays.asList(
                q("Lien Pitch Deck", QuestionType.FILE, true, 0, null),
                q("Besoins financement", QuestionType.TEXT, true, 1, null)));

        // Youssef: COMPLETED (winner) — all evals by all 3 evaluators in both rounds
        Application a1 = applicationRepository.save(Application.builder()
                .session(sessClosed).candidate(s1).currentRound(cR2)
                .status(ApplicationStatus.COMPLETED).build());
        Project p1 = projectRepository.save(Project.builder()
                .title("DataMind AI").description("Analyse prédictive industrie 4.0.")
                .domain("IA").githubUrl("https://github.com/youssef/datamind")
                .owner(s1).round(cR2).status(ProjectStatus.ACCEPTED)
                .submittedAt(LocalDateTime.now().minusMonths(7)).build());
        eval(a1, p1, evalTech, cR1, 85, "Excellent concept technique.");
        eval(a1, p1, evalBiz,  cR1, 78, "Business model prometteur.");
        eval(a1, p1, evalGen,  cR1, 82, "Bon potentiel global.");
        eval(a1, p1, evalTech, cR2, 90, "Prototype impressionnant.");
        eval(a1, p1, evalBiz,  cR2, 92, "Marché porteur, pitch convaincant.");
        eval(a1, p1, evalGen,  cR2, 88, "Projet mûr pour l'incubation.");

        // Fatma: ELIMINATED at Round 2 — fully evaluated both rounds
        Application a2 = applicationRepository.save(Application.builder()
                .session(sessClosed).candidate(s2).currentRound(cR2)
                .status(ApplicationStatus.ELIMINATED_ROUND_2).build());
        Project p2 = projectRepository.save(Project.builder()
                .title("ChatBot HR").description("Assistant RH virtuel.")
                .domain("IA & RH").owner(s2).round(cR2).status(ProjectStatus.REJECTED)
                .submittedAt(LocalDateTime.now().minusMonths(8)).build());
        eval(a2, p2, evalTech, cR1, 70, "Intéressant mais manque de détails.");
        eval(a2, p2, evalBiz,  cR1, 65, "Marché saturé.");
        eval(a2, p2, evalGen,  cR1, 72, "Passe avec réserve.");
        eval(a2, p2, evalTech, cR2, 45, "Architecture non viable.");
        eval(a2, p2, evalBiz,  cR2, 40, "Business case faible.");
        eval(a2, p2, evalGen,  cR2, 50, "Éliminé.");

        // ══════════════════════════════════════════════════════════════
        // 3. SESSION IN_PROGRESS — R1 done, R2 active, R3 upcoming
        //    Tests: eval workflow, fullyEvaluated check, admin override,
        //           jury president results, round final badge
        // ══════════════════════════════════════════════════════════════
        Session sessActive = sessionRepository.save(Session.builder()
                .name("Incubation GreenTech 2025")
                .description("Accompagnement startups à impact environnemental.")
                .startDate(LocalDate.now().minusMonths(2))
                .endDate(LocalDate.now().plusMonths(3))
                .status(SessionStatus.IN_PROGRESS).build());

        Round aR1 = roundRepository.save(Round.builder().session(sessActive).name("Évaluation du Concept")
                .orderIndex(1).roundNumber(1).status(RoundStatus.COMPLETED)
                .evaluators(allEvals).passingCandidatesCount(4)
                .juryPresident(evalGen).selectionValidated(true).selectionFinalized(true)
                .deadline(LocalDate.now().minusMonths(1)).build());
        Round aR2 = roundRepository.save(Round.builder().session(sessActive).name("Prototype Technique")
                .orderIndex(2).roundNumber(2).status(RoundStatus.ACTIVE)
                .evaluators(techEvals).passingCandidatesCount(2)
                .juryPresident(evalTech)
                .deadline(LocalDate.now().plusDays(14)).build());
        Round aR3 = roundRepository.save(Round.builder().session(sessActive).name("Go To Market")
                .orderIndex(3).roundNumber(3).status(RoundStatus.UPCOMING)
                .evaluators(Set.of(evalBiz)).passingCandidatesCount(1)
                .juryPresident(evalBiz)
                .deadline(LocalDate.now().plusMonths(2)).build());

        addQs(aR1, Arrays.asList(
                q("Titre du projet GreenTech", QuestionType.TEXT, true, 0, null),
                q("Impact écologique", QuestionType.TEXTAREA, true, 1, null),
                q("Technologie clé", QuestionType.RADIO, true, 2, "IoT,Matériaux,Énergie renouvelable,Autre")));
        addQs(aR2, Arrays.asList(
                q("Lien Démo / Vidéo", QuestionType.VIDEO_URL, false, 0, null)));
        addQs(aR3, Arrays.asList(
                q("Stratégie acquisition client", QuestionType.TEXTAREA, true, 0, null),
                q("Canaux de distribution", QuestionType.CHECKBOX, true, 1, "B2B Direct,Partenariats,Réseaux Sociaux,Autre")));

        List<SessionQuestion> gQ1 = questionRepository.findByRoundIdOrderByOrderIndexAsc(aR1.getId());
        List<SessionQuestion> gQ2 = questionRepository.findByRoundIdOrderByOrderIndexAsc(aR2.getId());

        // Ahmed: ACCEPTED_ROUND_2 — fully evaluated by both techEvals in R2
        //   → Tests: fullyEvaluated = true, admin can accept
        Application a3 = applicationRepository.save(Application.builder()
                .session(sessActive).candidate(s3).currentRound(aR2)
                .status(ApplicationStatus.ACCEPTED_ROUND_2).build());
        answer(a3, gQ1.get(0), "SolarFlow"); answer(a3, gQ1.get(1), "Panneaux solaires optimisés.");
        answer(a3, gQ1.get(2), "Énergie renouvelable");
        answer(a3, gQ2.get(0), "https://youtube.com/solarflow");
        Project p3 = projectRepository.save(Project.builder()
                .title("SolarFlow").description("Monitoring solaire intelligent.")
                .domain("Énergie").githubUrl("https://github.com/ahmed/solarflow")
                .owner(s3).round(aR2).status(ProjectStatus.UNDER_REVIEW)
                .submittedAt(LocalDateTime.now().minusDays(2)).build());
        // R1 evals (all 3)
        eval(a3, p3, evalTech, aR1, 80, "Bon potentiel technique.");
        eval(a3, p3, evalBiz,  aR1, 75, "Marché niche mais viable.");
        eval(a3, p3, evalGen,  aR1, 78, "Concept solide.");
        // R2 evals (both techEvals → fullyEvaluated = true)
        eval(a3, p3, evalTech, aR2, 85, "Prototype fonctionnel et bien conçu.");
        eval(a3, p3, evalGen,  aR2, 82, "Bonne démonstration technique.");

        // Nour: ACCEPTED_ROUND_2 — only 1 of 2 techEvals evaluated in R2
        //   → Tests: fullyEvaluated = false (1/2), admin CANNOT accept
        Application a4 = applicationRepository.save(Application.builder()
                .session(sessActive).candidate(s4).currentRound(aR2)
                .status(ApplicationStatus.ACCEPTED_ROUND_2).build());
        answer(a4, gQ1.get(0), "EcoTrack"); answer(a4, gQ1.get(1), "Suivi consommation carbone.");
        answer(a4, gQ1.get(2), "Autre");
        Project p4 = projectRepository.save(Project.builder()
                .title("EcoTrack App").description("App mobile tracking carbone.")
                .domain("Mobile & Écologie").owner(s4).round(aR2).status(ProjectStatus.ACCEPTED)
                .submittedAt(LocalDateTime.now().minusDays(5)).build());
        // R1 evals (all 3)
        eval(a4, p4, evalTech, aR1, 88, "Excellent concept.");
        eval(a4, p4, evalBiz,  aR1, 90, "Business model très clair.");
        eval(a4, p4, evalGen,  aR1, 85, "Potentiel énorme.");
        // R2 evals (only evalTech, missing evalGen → fullyEvaluated = false)
        eval(a4, p4, evalTech, aR2, 78, "Prototype correct mais manque de polish.");

        // Omar: ACCEPTED_ROUND_2 — NO evaluations yet in R2
        //   → Tests: fullyEvaluated = false (0/2), eval buttons active
        Application a5 = applicationRepository.save(Application.builder()
                .session(sessActive).candidate(s5).currentRound(aR2)
                .status(ApplicationStatus.ACCEPTED_ROUND_2).build());
        answer(a5, gQ1.get(0), "WindTech"); answer(a5, gQ1.get(1), "Micro-éoliennes urbaines.");
        answer(a5, gQ1.get(2), "Énergie renouvelable");
        // R1 evals only
        eval(a5, null, evalTech, aR1, 72, "Concept original mais faisabilité incertaine.");
        eval(a5, null, evalBiz,  aR1, 68, "Marché difficile.");
        eval(a5, null, evalGen,  aR1, 74, "Passe avec réserve.");

        // Ines: ELIMINATED_ROUND_1
        //   → Tests: history preserved, no eval buttons
        Application a6 = applicationRepository.save(Application.builder()
                .session(sessActive).candidate(s6).currentRound(aR1)
                .status(ApplicationStatus.ELIMINATED_ROUND_1).build());
        answer(a6, gQ1.get(0), "GreenPlast"); answer(a6, gQ1.get(1), "Recyclage plastique.");
        eval(a6, null, evalTech, aR1, 40, "Trop vague techniquement.");
        eval(a6, null, evalBiz,  aR1, 35, "Pas de business model.");
        eval(a6, null, evalGen,  aR1, 42, "Éliminé.");

        // ══════════════════════════════════════════════════════════════
        // 4. SESSION OPEN — Round 1 active, candidates applying
        //    Tests: PENDING status, student submission, UPCOMING round
        // ══════════════════════════════════════════════════════════════
        Session sessOpen = sessionRepository.save(Session.builder()
                .name("FinTech Challenge Automne")
                .description("Idées innovantes pour la finance de demain.")
                .startDate(LocalDate.now().minusDays(5))
                .endDate(LocalDate.now().plusMonths(2))
                .status(SessionStatus.OPEN).build());

        Round oR1 = roundRepository.save(Round.builder().session(sessOpen).name("Phase d'Inscription")
                .orderIndex(1).roundNumber(1).status(RoundStatus.ACTIVE)
                .evaluators(allEvals).passingCandidatesCount(3)
                .juryPresident(evalGen)
                .deadline(LocalDate.now().plusDays(30)).build());
        Round oR2 = roundRepository.save(Round.builder().session(sessOpen).name("Pitch Day")
                .orderIndex(2).roundNumber(2).status(RoundStatus.UPCOMING)
                .evaluators(techEvals).passingCandidatesCount(1)
                .juryPresident(evalTech)
                .deadline(LocalDate.now().plusMonths(2)).build());

        addQs(oR1, Arrays.asList(
                q("Nom startup FinTech", QuestionType.TEXT, true, 0, null),
                q("Problème résolu", QuestionType.TEXTAREA, true, 1, null),
                q("Marché ciblé", QuestionType.CHECKBOX, true, 2, "B2B,B2C,B2B2C,Institutionnel")));
        addQs(oR2, Arrays.asList(
                q("Lien présentation", QuestionType.FILE, true, 0, null)));

        List<SessionQuestion> fQ = questionRepository.findByRoundIdOrderByOrderIndexAsc(oR1.getId());

        // Ali: PENDING — just applied, waiting for acceptance
        Application a7 = applicationRepository.save(Application.builder()
                .session(sessOpen).candidate(s7).currentRound(oR1)
                .status(ApplicationStatus.PENDING).build());
        answer(a7, fQ.get(0), "PaySmart"); answer(a7, fQ.get(1), "Paiement QR Code innovant.");
        answer(a7, fQ.get(2), "B2C,B2B");

        // Mariem: REJECTED — invalid application
        Application a8 = applicationRepository.save(Application.builder()
                .session(sessOpen).candidate(s8).currentRound(null)
                .status(ApplicationStatus.REJECTED).build());
        answer(a8, fQ.get(0), "Test App"); answer(a8, fQ.get(1), "Rien");

        // ══════════════════════════════════════════════════════════════
        // DONE
        // ══════════════════════════════════════════════════════════════
        log.info("✅ Jeu de données complet créé !");
        log.info("📧 Comptes:");
        log.info("   Admin:       admin@enicarthage.tn / Admin@2024");
        log.info("   Éval Tech:   sami.tech@enicarthage.tn / Eval@2024 (Jury Président R2 GreenTech + R2 FinTech)");
        log.info("   Éval Biz:    amira.biz@enicarthage.tn / Eval@2024 (Jury Président R3 GreenTech)");
        log.info("   Éval Gen:    karim.gen@enicarthage.tn / Eval@2024 (Jury Président R1 GreenTech + R1 FinTech)");
        log.info("   Candidats:   youssef@enicarthage.tn (COMPLETED), ahmed@enicarthage.tn (R2 fully eval'd)");
        log.info("                nour@enicarthage.tn (R2 partial 1/2), omar@enicarthage.tn (R2 no evals)");
        log.info("                ali@enicarthage.tn (PENDING), mariem@enicarthage.tn (REJECTED)");
        log.info("   Mot de passe étudiants: Student@2024");
    }

    // ── Helpers ──────────────────────────────────────────────────────

    private User mkUser(String first, String last, String email, Role role, String specialty) {
        if (!userRepository.existsByEmail(email)) {
            String pwd = role == Role.ADMIN ? "Admin@2024" : (role == Role.EVALUATOR ? "Eval@2024" : "Student@2024");
            return userRepository.save(User.builder()
                    .firstName(first).lastName(last).email(email)
                    .password(passwordEncoder.encode(pwd))
                    .role(role).specialty(specialty).enabled(true).blocked(false).build());
        }
        return userRepository.findByEmail(email).orElseThrow();
    }

    private void eval(Application app, Project proj, User evaluator, Round round, int score, String comment) {
        evaluationRepository.save(Evaluation.builder()
                .application(app).project(proj).evaluator(evaluator).round(round)
                .score(score).comment(comment).recommendation(score >= 70 ? "Accepter" : "Refuser")
                .build());
    }

    private void answer(Application app, SessionQuestion question, String text) {
        answerRepository.save(QuestionnaireAnswer.builder()
                .application(app).question(question).answer(text).build());
    }

    private SessionQuestion q(String label, QuestionType type, boolean required, int idx, String options) {
        return SessionQuestion.builder()
                .label(label).type(type).required(required).orderIndex(idx).options(options).build();
    }

    private void addQs(Round round, List<SessionQuestion> questions) {
        questions.forEach(q -> q.setRound(round));
        questionRepository.saveAll(questions);
    }
}
